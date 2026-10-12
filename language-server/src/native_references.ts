import * as scriptfiles from './as_parser';
import * as symbols from './symbols';
import { ResponseError } from 'vscode-languageserver';

type NativeSymbol = ReturnType<typeof symbols.GetCppSymbolInfo>;
const modules = new WeakMap<scriptfiles.ASModule, {
    semantic: scriptfiles.ASSemanticSymbol[], document: any,
    references: { location: any, symbol: NativeSymbol, text: string }[]
}>();

/** Index semantic native references once per resolved module, including UE aliases and offline receiver inference. */
export async function FindNativeReferences(names: string[], cppName: string, cancelled: () => boolean) {
    const candidates = new Set(names);
    const result = [];
    const snapshots = [];
    for (const module of scriptfiles.GetAllLoadedModules()) {
        if (cancelled()) throw new ResponseError(-32800, 'Search cancelled');
        if (!module.exists) continue;
        scriptfiles.LoadAndParseModule(module);
        scriptfiles.PostProcessModuleTypes(module);
        scriptfiles.ResolveModule(module);
        let index = modules.get(module);
        if (!index || index.semantic !== module.semanticSymbols || index.document !== module.textDocument) {
            const references = [];
            for (const candidate of module.semanticSymbols) {
                if (candidate.isDeclaration) continue;
                const symbol = symbols.GetCppSymbolInfo(module, module.getPosition(candidate.start));
                if (symbol) references.push({ location: module.getLocationRange(candidate.start, candidate.end), symbol,
                    text: module.content.substring(candidate.start, candidate.end) });
            }
            index = { semantic: module.semanticSymbols, document: module.textDocument, references };
            modules.set(module, index);
        }
        snapshots.push({ module, index });
        for (const reference of index.references) {
            if ([reference.symbol, ...(reference.symbol.alternatives || [])].some(value =>
                value.cppName === cppName || candidates.has(value.symbolName))) result.push(reference);
        }
        await new Promise(resolve => setImmediate(resolve));
    }
    if (cancelled()) throw new ResponseError(-32800, 'Search cancelled');
    if (snapshots.some(({ module, index }) => !module.exists || !module.resolved ||
        module.semanticSymbols !== index.semantic || module.textDocument !== index.document))
        throw new ResponseError(-32801, 'AngelScript sources changed during search; retry Find Usages');
    return result;
}
