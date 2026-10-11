const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const { transformSync } = require('esbuild');
const Module = require('node:module');
function database() {
    const filename = resolve(__dirname, '../src/database.ts');
    const module = new Module(filename);
    module._compile(transformSync(readFileSync(filename, 'utf8'), { loader: 'ts', format: 'cjs' }).code, filename);
    const db = module.exports;
    db.AddTypesFromUnreal(JSON.parse(readFileSync(resolve(__dirname, 'fixtures/native-types.json'))));
    return db;
}
test('nested and qualified template types retain every argument and qualifier', () => {
    const db = database();
    assert.deepEqual(db.ParseTemplateType('TMap<int,TArray<TMap<int,FName>>>'), {
        base: 'TMap', subtypes: ['int', 'TArray<TMap<int,FName>>'],
    });
    assert.equal(db.ReplaceTemplateType('const TMap<K,TArray<V>>&', ['K','V'], ['FName','FString']),
        'const TMap<FName,TArray<FString>>&');
    const outer = db.LookupType(null, 'TMap<FName,TArray<TMap<int,FString>>>');
    assert.equal(outer.templateSubTypes.length, 2);
    const iterator = db.LookupType(null, outer.findFirstSymbol('Iterator').returnType);
    assert.equal(iterator.findFirstSymbol('GetValue').returnType, 'TArray<TMap<int,FString>>&');
    assert.equal(iterator.isStruct, true);
});
test('template member instances preserve binding and declaration metadata', () => {
    const db = database();
    db.AddTypesFromUnreal({ TProbe: { isStruct: true, subtypes: ['T'], properties: {}, methods: [
        { name: 'Call', return: 'T', ufunction: true, unrealname: 'NativeCall' },
    ] }});
    const inst = db.LookupType(null, 'TProbe<FName>');
    const method = inst.findFirstSymbol('Call');
    assert.equal(method.unrealName, 'NativeCall');
    assert.equal(method.isUFunction, true);
    assert.equal(method.containingType, inst);
});
test('a refreshed UE database exposes newly added template functions on existing uses', () => {
    const db = database();
    const before = db.LookupType(null, 'TMapIterator<int,FName>');
    assert.equal(before.findFirstSymbol('LaterAdded'), null);
    const update = JSON.parse(readFileSync(resolve(__dirname, 'fixtures/native-types.json'))).TMapIterator;
    update.methods.push({ name: 'LaterAdded', return: 'V&' });
    db.AddTypesFromUnreal({ TMapIterator: update });
    const after = db.LookupType(null, 'TMapIterator<int,FName>');
    assert.notEqual(before, after);
    assert.equal(after.findFirstSymbol('LaterAdded').returnType, 'FName&');
});
