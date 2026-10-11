const { test } = require('node:test');
const assert = require('node:assert/strict');
const { spawn, execFileSync } = require('node:child_process');
const { createServer } = require('node:net');
const { mkdtempSync, mkdirSync, writeFileSync, rmSync } = require('node:fs');
const { tmpdir } = require('node:os');
const { join, resolve } = require('node:path');
const { pathToFileURL, fileURLToPath } = require('node:url');

test('standalone client defaults match VS Code and preserve nested partial updates', () => {
    const { readFileSync } = require('node:fs');
    const { transformSync } = require('esbuild');
    const Module = require('node:module');
    const source = readFileSync(resolve(__dirname, '../src/client_settings.ts'), 'utf8');
    const compiled = transformSync(source, { loader: 'ts', format: 'cjs' }).code;
    const module = new Module(__filename);
    module._compile(compiled, __filename);
    const { defaultClientSettings, mergeClientSettings } = module.exports;
    const properties = require('../../package.json').contributes.configuration.properties;
    for (const [key, property] of Object.entries(properties)) {
        const parts = key.split('.').slice(1);
        const value = parts.reduce((settings, part) => settings[part], defaultClientSettings);
        assert.deepEqual(value, property.default, key);
    }
    const merged = mergeClientSettings({ unrealConnectionPort: 12345 }, { inlayHints: { inlayHintsEnabled: false } });
    assert.equal(merged.unrealConnectionPort, 12345);
    assert.equal(merged.inlayHints.inlayHintsEnabled, false);
    assert.equal(merged.inlayHints.parameterHintsForConstants, true);
    assert.equal(defaultClientSettings.inlayHints.inlayHintsEnabled, true);
});

test('shared grammar preserves colon and equals named arguments, ternaries and namespaces', () => {
    const parser = require('../pegjs/angelscript.js');
    const types = require('../grammar/node_types.js');
    function parse(value) { return parser.parse(value, { startRule: 'start', endsWithSemicolon: true }); }
    function nodes(root, type) {
        if (!root) return [];
        return (root.type === type ? [root] : []).concat((root.children || []).flatMap(child => nodes(child, type)));
    }
    for (const separator of [':', '=']) {
        const ast = parse(`return DefaultTarget(Third${separator} 7, First${separator} 19)`);
        const args = nodes(ast, types.NamedArgument);
        assert.deepEqual(args.map(arg => arg.children[0].value), ['Third', 'First']);
        assert.deepEqual(args.map(arg => arg.children[1].value), ['7', '19']);
    }
    const nested = parse('return DefaultTarget(Third: Flag ? Inner(Value: 7, Other: 8) : 9, First: 19)');
    assert.deepEqual(nodes(nested, types.NamedArgument).map(arg => arg.children[0].value), ['Third', 'Value', 'Other', 'First']);
    assert.equal(nodes(nested, types.TernaryOperation).length, 1);
    const namespace = parse('return Target(Namespace::Value, Flag ? 8 : 9)');
    assert.equal(nodes(namespace, types.NamedArgument).length, 0);
    assert.equal(nodes(namespace, types.NamespaceAccess).length, 1);
    assert.equal(nodes(namespace, types.TernaryOperation).length, 1);
});

for (const withUnreal of [false, true])
test(`Rider stdio language features ${withUnreal ? 'with UE diagnostics' : 'without a running UE editor'}`, { timeout: 30000 }, async () => {
    const folder = mkdtempSync(join(tmpdir(), 'rider-angelscript-'));
    const script = join(folder, 'Script 中文 # with spaces');
    mkdirSync(script);
    const filename = join(script, 'Example.as');
    const text = [
        'int DefaultTarget(int First = 1, int Second = 2, int Third = 3)',
        '{ return First + Second + Third; }',
        'int DefaultNamed()',
        '{ return DefaultTarget(Third: 7, First: 19); }',
        'int NestedNamed(bool Flag)',
        '{ return DefaultTarget(Third: Flag ? DefaultTarget(First: 7, Third: 8) : 9, First: 19); }',
        'class AExampleActor',
        '{',
        '    int Count;',
        '    void Tick(float DeltaSeconds)',
        '    {',
        '        int Local = 7;',
        '        Count = Local;',
        '        Count += 1;',
        '    }',
        '}',
        'asset ExampleAsset of AExampleActor',
        '{',
        '    Count = 1;',
        '}',
        'int CrossFile() { return RemoteValue(); }',
        'void UseActor(AExampleActor Actor) { Actor.Tick(1.0f); }',
    ].join('\n');
    writeFileSync(filename, text);
    const remoteFilename = join(script, 'Remote.as');
    writeFileSync(remoteFilename, 'int RemoteValue() { return 42; }');
    const sockets = new Set();
    let markConnected;
    const connected = new Promise(resolve => { markConnected = resolve; });
    const unreal = createServer(socket => {
        sockets.add(socket);
        socket.on('error', () => {});
        socket.on('close', () => sockets.delete(socket));
        // Keep the database pending while testing script features, then deliver it later.
        markConnected();
    });
    await new Promise(resolve => unreal.listen(0, '127.0.0.1', resolve));
    const unrealPort = unreal.address().port;
    if (!withUnreal) await new Promise(resolve => unreal.close(resolve));
    const child = spawn(process.execPath, [resolve(__dirname, '../dist/server.js'), '--stdio'], { stdio: 'pipe' });
    let stderr = '';
    child.stderr.on('data', data => { stderr += data; });
    const pending = new Map();
    const notifications = [];
    const notificationWaiters = new Set();
    let buffer = Buffer.alloc(0);
    let nextId = 0;
    child.stdout.on('data', chunk => {
        buffer = Buffer.concat([buffer, chunk]);
        while (true) {
            const headerEnd = buffer.indexOf('\r\n\r\n');
            if (headerEnd < 0) break;
            const header = buffer.subarray(0, headerEnd).toString();
            const length = Number(/Content-Length:\s*(\d+)/i.exec(header)?.[1]);
            assert.ok(Number.isFinite(length), header);
            if (buffer.length < headerEnd + 4 + length) break;
            const message = JSON.parse(buffer.subarray(headerEnd + 4, headerEnd + 4 + length));
            buffer = buffer.subarray(headerEnd + 4 + length);
            if (message.id != null && pending.has(message.id)) {
                pending.get(message.id)(message);
                pending.delete(message.id);
            } else if (message.method) {
                notifications.push(message);
                for (const notify of notificationWaiters) notify();
            }
        }
    });
    function send(message) {
        const payload = Buffer.from(JSON.stringify({ jsonrpc: '2.0', ...message }));
        child.stdin.write(`Content-Length: ${payload.length}\r\n\r\n`);
        child.stdin.write(payload);
    }
    async function request(method, params) {
        const id = ++nextId;
        const response = new Promise((resolve, reject) => {
            const timer = setTimeout(() => reject(new Error(`${method} timed out; ${stderr}`)), 8000);
            pending.set(id, message => { clearTimeout(timer); resolve(message); });
        });
        send({ id, method, params });
        const message = await response;
        assert.equal(message.error, undefined, JSON.stringify(message.error));
        return message.result;
    }
    async function notification(predicate) {
        const existing = notifications.find(predicate);
        if (existing) return existing;
        return new Promise((resolve, reject) => {
            const timer = setTimeout(() => { notificationWaiters.delete(check); reject(new Error(`Notification timed out: ${JSON.stringify(notifications)}; ${stderr}`)); }, 5000);
            function check() {
                const found = notifications.find(predicate);
                if (!found) return;
                clearTimeout(timer);
                notificationWaiters.delete(check);
                resolve(found);
            }
            notificationWaiters.add(check);
        });
    }
    try {
        const uri = pathToFileURL(script).toString();
        const result = await request('initialize', {
            processId: process.pid, rootUri: uri,
            workspaceFolders: [{ uri, name: 'Script' }], capabilities: {},
            initializationOptions: { unrealAngelscript: { unrealConnectionPort: unrealPort } },
        });
        assert.equal(result.capabilities.definitionProvider, true);
        assert.equal(result.capabilities.foldingRangeProvider, true);
        assert.equal(result.capabilities.selectionRangeProvider, true);
        assert.equal(result.capabilities.completionProvider.resolveProvider, true);
        const expectedSemanticTypes = require('node:fs').readFileSync(resolve(__dirname, '../src/semantic_highlighting.ts'), 'utf8')
            .match(/export let SemanticTypeList[^]*?\];/)[0].match(/"([a-z_]+)"/g).map(value => `as_${JSON.parse(value)}`);
        assert.deepEqual(result.capabilities.semanticTokensProvider.legend.tokenTypes, expectedSemanticTypes);
        if (withUnreal) await Promise.race([connected, new Promise((_, reject) => {
            const timer = setTimeout(() => reject(new Error('Configured Unreal port was not used')), 3000);
            timer.unref();
        })]);
        send({ method: 'initialized', params: {} });
        const status = await notification(message => message.method === 'angelscript/unrealStatus'
            && message.params.state === (withUnreal ? 'loading' : 'disconnected'));
        assert.equal(status.params.host, '127.0.0.1');
        assert.equal(status.params.port, unrealPort);
        send({ method: 'workspace/didChangeConfiguration', params: {
            settings: { UnrealAngelscript: { inlayHints: { inlayHintsEnabled: false } } },
        }});
        send({ method: 'textDocument/didOpen', params: { textDocument: {
            uri: pathToFileURL(filename).toString(), languageId: 'angelscript', version: 1, text,
        }}});
        const document = { uri: pathToFileURL(filename).toString() };
        function assertDocumentUri(uri) {
            assert.ok(!uri.includes(' '), `Unescaped space in ${uri}`);
            assert.equal(new URL(uri).hash, '', `Unescaped # in ${uri}`);
            // vscode-uri normalizes Windows drive letters; both forms identify the same file.
            const normalize = value => process.platform === 'win32' ? value.toLowerCase() : value;
            assert.equal(normalize(fileURLToPath(uri)), normalize(filename));
        }
        const at = (line, word, shift = 1) => ({ textDocument: document,
            position: { line, character: text.split('\n')[line].indexOf(word) + shift } });
        const symbols = await request('textDocument/documentSymbol', { textDocument: document });
        assert.ok(symbols.some(symbol => symbol.name === 'AExampleActor'), JSON.stringify(symbols));
        assert.ok(symbols.some(symbol => symbol.name === 'DefaultNamed()'), JSON.stringify(symbols));
        const definition = await request('textDocument/definition', at(3, 'DefaultTarget'));
        if (Array.isArray(definition)) assert.equal(definition.length, 1, JSON.stringify(definition));
        assert.equal((Array.isArray(definition) ? definition[0] : definition).range.start.line, 0);
        assertDocumentUri((Array.isArray(definition) ? definition[0] : definition).uri);
        for (const [line, name, targetLine] of [[12, 'Local', 11], [12, 'Count', 8], [21, 'Tick', 9]]) {
            const result = await request('textDocument/definition', at(line, name));
            const target = Array.isArray(result) ? result[0] : result;
            assert.ok(target, `${line}:${name}: ${JSON.stringify(result)}`);
            assert.equal(target.range.start.line, targetLine);
            assertDocumentUri(target.uri);
        }
        const crossFile = await request('textDocument/definition', at(20, 'RemoteValue'));
        const remoteTarget = Array.isArray(crossFile) ? crossFile[0] : crossFile;
        assert.ok(remoteTarget, JSON.stringify(crossFile));
        assert.equal(fileURLToPath(remoteTarget.uri).toLowerCase(), remoteFilename.toLowerCase());
        for (const [line, name] of [[3, 'DefaultTarget'], [12, 'Local']]) {
            const hover = await request('textDocument/hover', at(line, name));
            assert.ok(JSON.stringify(hover).includes(name), `${line}:${name}: ${JSON.stringify(hover)}`);
        }
        const signature = await request('textDocument/signatureHelp', at(3, '19', 0));
        assert.ok(signature.signatures[0].label.includes('DefaultTarget'), JSON.stringify(signature));
        const completion = await request('textDocument/completion', at(13, 'Count', 3));
        assert.ok(completion.some(item => item.label === 'Count'), JSON.stringify(completion));
        const references = await request('textDocument/references', { ...at(12, 'Local'), context: { includeDeclaration: true } });
        assert.ok(references.length >= 2, JSON.stringify(references));
        for (const reference of references) assertDocumentUri(reference.uri);
        const rename = await request('textDocument/rename', { ...at(12, 'Local'), newName: 'RenamedLocal' });
        const edits = Object.values(rename.changes || {}).flat().concat((rename.documentChanges || []).flatMap(change => change.edits || []));
        assert.equal(edits.filter(edit => edit.newText === 'RenamedLocal').length, 2, JSON.stringify(rename));
        assert.equal(Object.keys(rename.changes || {}).length, 1);
        assertDocumentUri(Object.keys(rename.changes)[0]);
        const remoteUri = pathToFileURL(remoteFilename).toString();
        send({ method: 'textDocument/didOpen', params: { textDocument: {
            uri: remoteUri, languageId: 'angelscript', version: 1,
            text: 'int RemoteValue() { return 42; }',
        }}});
        const remoteReferences = await request('textDocument/references', {
            ...at(20, 'RemoteValue'), context: { includeDeclaration: true },
        });
        assert.equal(remoteReferences.length, 2, JSON.stringify(remoteReferences));
        const remoteRename = await request('textDocument/rename', {
            textDocument: { uri: remoteUri }, position: { line: 0, character: 7 }, newName: 'RenamedRemote',
        });
        assert.equal(Object.keys(remoteRename.changes || {}).length, 2, JSON.stringify(remoteRename));
        assert.equal(Object.values(remoteRename.changes || {}).flat()
            .filter(edit => edit.newText === 'RenamedRemote').length, 2, JSON.stringify(remoteRename));
        const semantic = await request('textDocument/semanticTokens/full', { textDocument: document });
        assert.ok(semantic.data.length > 0 && semantic.data.length % 5 === 0, JSON.stringify(semantic));
        const folds = await request('textDocument/foldingRange', { textDocument: document });
        assert.ok(folds.some(range => range.startLine <= 10 && range.endLine >= 13), JSON.stringify(folds));
        assert.ok(folds.every(range => range.startLine < range.endLine));
        const selections = await request('textDocument/selectionRange', {
            textDocument: document, positions: [at(12, 'Local').position, at(3, 'Third').position],
        });
        for (const [index, name] of ['Local', 'Third'].entries()) {
            let selection = selections[index];
            const lines = text.split('\n');
            assert.equal(lines[selection.range.start.line].slice(selection.range.start.character, selection.range.end.character), name);
            let depth = 0;
            while (selection.parent) {
                const key = position => position.line * 10000 + position.character;
                assert.ok(key(selection.parent.range.start) <= key(selection.range.start));
                assert.ok(key(selection.parent.range.end) >= key(selection.range.end));
                selection = selection.parent;
                ++depth;
            }
            assert.ok(depth >= 2);
        }
        const stopped = { start: { line: 13, character: 8 }, end: { line: 13, character: 8 } };
        const inline = await request('textDocument/inlineValue', {
            textDocument: document, range: stopped, context: { frameId: 0, stoppedLocation: stopped },
        });
        for (const name of ['DeltaSeconds', 'Local', 'Count'])
            assert.ok(inline.some(value => value.variableName === name), JSON.stringify(inline));

        if (withUnreal) {
        // An actual compiler error must still reach the IDE after removal of local grammar diagnostics.
        // Simulate the UE protocol's Diagnostics frame, including a Unicode message and a spaced path.
        const int = value => { const buffer = Buffer.alloc(4); buffer.writeInt32LE(value); return buffer; };
        const string = value => { const buffer = Buffer.from(value + '\0', 'utf8'); return Buffer.concat([int(buffer.length), buffer]); };
        const edited = text + '\nint UnsavedTarget() { return 11; }\nint UnsavedCaller() { return UnsavedTarget(); }';
        send({ method: 'textDocument/didChange', params: {
            textDocument: { ...document, version: 2 }, contentChanges: [{ text: edited }],
        }});
        await request('textDocument/hover', at(12, 'Local'));
        for (const socket of sockets) socket.write(Buffer.from([0, 0, 0, 0, 26]));
        await notification(message => message.method === 'angelscript/unrealStatus' && message.params.state === 'ready');
        const afterTypes = await request('textDocument/definition', {
            textDocument: document, position: { line: text.split('\n').length + 1, character: 35 },
        });
        const unsavedTarget = Array.isArray(afterTypes) ? afterTypes[0] : afterTypes;
        assert.ok(unsavedTarget, JSON.stringify(afterTypes));
        assert.equal(unsavedTarget.range.start.line, text.split('\n').length);
        assertDocumentUri(unsavedTarget.uri);
        const errorText = '测试 compiler error: expected expression';
        const body = Buffer.concat([string(filename.replaceAll('\\', '/')), int(1), string(errorText), int(4), int(10), int(1), int(0)]);
        for (const socket of sockets) socket.write(Buffer.concat([int(body.length), Buffer.from([0]), body]));
        const diagnostics = await notification(message => message.method === 'textDocument/publishDiagnostics'
            && message.params.diagnostics.some(diagnostic => diagnostic.message === errorText));
        const error = diagnostics.params.diagnostics.find(diagnostic => diagnostic.message === errorText);
        assertDocumentUri(diagnostics.params.uri);
        assert.equal(error.severity, 1);
        assert.equal(error.range.start.line, 3);
        assert.ok(error.range.end.character <= text.split('\n')[3].length);

        // UE asset edits must wait for the client's asynchronous applyEdit acknowledgement before saving.
        function replaceAsset(content) {
            const payload = Buffer.concat([string('ExampleAsset'), int(1), string(content)]);
            for (const socket of sockets) socket.write(Buffer.concat([int(payload.length), Buffer.from([35]), payload]));
        }
        replaceAsset('Count = 42;');
        const apply = await notification(message => message.method === 'workspace/applyEdit');
        assertDocumentUri(Object.keys(apply.params.edit.changes)[0]);
        assert.ok(JSON.stringify(apply.params.edit).includes('Count = 42;'));
        await request('textDocument/hover', at(12, 'Local')); // Allow other requests while applyEdit is pending.
        assert.equal(notifications.filter(message => message.method === 'angelscript/wantSave').length, 0);
        send({ id: apply.id, result: { applied: true } });
        const save = await notification(message => message.method === 'angelscript/wantSave');
        // Positional JSON-RPC parameters: the single parameter is the URI list.
        assertDocumentUri(save.params[0][0]);
        replaceAsset('Count = 99;');
        const rejected = await notification(message => message.method === 'workspace/applyEdit' && message.id !== apply.id);
        send({ id: rejected.id, result: { applied: false, failureReason: 'read only' } });
        const failure = await notification(message => message.method === 'window/showMessageRequest' && message.params.message.includes('read only'));
        send({ id: failure.id, result: null });
        assert.equal(notifications.filter(message => message.method === 'angelscript/wantSave').length, 1);
        for (const socket of sockets) socket.destroy();
        await notification(message => message.method === 'angelscript/unrealStatus' && message.params.state === 'disconnected');
        }
        await request('shutdown', null);
        send({ method: 'exit' });
        await new Promise(resolve => child.once('exit', resolve));
        assert.equal(child.exitCode, 0, stderr);
        assert.equal(stderr, '');
    } finally {
        child.kill();
        for (const socket of sockets) socket.destroy();
        if (unreal.listening) await new Promise(resolve => unreal.close(resolve));
        // This is solely the disposable directory created by this test.
        if (process.platform === 'win32') {
            const literal = `'${folder.replaceAll("'", "''")}'`;
            const shell = 'powershell.exe';
            const run = command => execFileSync(shell, ['-NoProfile', '-NonInteractive', '-Command', command]);
            run(`$target = Get-Item -LiteralPath ${literal}; if ($target.Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Unexpected reparse point' }; if (Get-ChildItem -LiteralPath ${literal} -Recurse -Attributes ReparsePoint) { throw 'Unexpected nested reparse point' }`);
            run(`Remove-Item -LiteralPath ${literal} -Recurse -ErrorAction Stop`);
            run(`if (Test-Path -LiteralPath ${literal}) { throw 'Temporary folder was not removed' }`);
        } else {
            rmSync(folder, { recursive: true });
        }
    }
});
