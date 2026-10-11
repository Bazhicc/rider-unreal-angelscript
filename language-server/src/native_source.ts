import * as scripts from './as_parser';
import * as db from './database';
import { Position } from 'vscode-languageserver';

/** Source hints are checked against indexed C++ declarations, never sent to the UE bridge. */
export function GetSourceCppSymbolInfo(module: scripts.ASModule, position: Position)
{
    const offset = module.getOffset(position);
    const symbol = module.getSymbolAtOrBefore(offset);
    if (symbol && symbol.type != scripts.ASSymbolType.UnknownError
        && symbol.type != scripts.ASSymbolType.Namespace) return null;
    const statement = module.getStatementAt(offset);
    const scope = module.getScopeAt(offset);
    if (!statement?.ast || !scope) return null;
    const relative = offset - statement.start_offset;
    const nodes: any[] = [];
    function visit(node: any) {
        if (!node || typeof node != 'object') return;
        if (typeof node.type == 'number' && node.start <= relative && relative <= node.end) nodes.push(node);
        for (const value of Object.values(node))
            if (Array.isArray(value)) value.forEach(visit);
            else if (value && typeof value == 'object') visit(value);
    }
    visit(statement.ast);
    const token = nodes.slice().reverse().find(node => typeof node.value == 'string'
        && /^[A-Za-z_][\w:]*$/.test(node.value));
    if (!token) return null;
    const name = module.content.substring(statement.start_offset + token.start, statement.start_offset + token.end)
        .split('::').pop();
    if (!/^[A-Za-z_]\w*$/.test(name)) return null;
    const candidate = (owner: string, kind: string, symbolName = name) => ({
        typeName: owner, symbolName, cppName: symbolName, kind,
        canNavigateViaUnreal: false, sourceOnly: true,
    });
    function nativeOwner(typename: string): string | null {
        if (!typename || typename == 'auto') return null;
        let type = db.LookupType(scope.getNamespace(), typename);
        const visited = new Set<string>();
        while (type?.declaredModule) {
            if (!type.supertype || visited.has(type.name)) return null;
            visited.add(type.name);
            typename = type.supertype;
            type = db.LookupType(type.namespace, typename);
        }
        if (type?.isPrimitive) return null;
        return db.CleanTypeName(typename).split('<', 1)[0];
    }
    function expressionType(expr: any, visited = new Set<any>()): string | null {
        if (!expr || visited.has(expr)) return null;
        visited.add(expr);
        const resolved = scripts.ResolveTypeFromExpression(scope, expr);
        if (resolved) return resolved.name;
        if (expr.type == scripts.node_types.Identifier) {
            for (let check = scope; check; check = check.parentscope) {
                const variable = check.variablesByName.get(expr.value);
                if (variable && (variable.isArgument || variable.isMember || variable.start_offset_name <= offset))
                    return variable.isAuto ? expressionType(variable.node_expression, visited) : variable.typename;
            }
            const property = scope.getParentType()?.findSymbols(expr.value).find(value => value instanceof db.DBProperty) as db.DBProperty;
            return property?.typename;
        }
        if (expr.type == scripts.node_types.This) return scope.getParentType()?.name;
        if (expr.type == scripts.node_types.CastOperation) return expr.children[0]?.value;
        if (expr.type == scripts.node_types.MemberAccess) {
            const owner = db.LookupType(scope.getNamespace(), expressionType(expr.children[0], visited));
            const property = owner?.findSymbols(expr.children[1]?.value).find(value => value instanceof db.DBProperty) as db.DBProperty;
            return property?.typename;
        }
        const method = scripts.ResolveFunctionFromExpression(scope, expr.children?.[0]);
        return expr.type == scripts.node_types.FunctionCall ? method?.returnType : null;
    }
    const isType = nodes.some(node => node.type == scripts.node_types.Typename)
        || nodes.some(node => node.superclass === token);
    if (isType) {
        // An AS declaration stays an AS target, even if it inherits a native class.
        const type = db.LookupType(scope.getNamespace(), token.value);
        if (type?.declaredModule || type?.isPrimitive) return null;
        return candidate(token.value.split('<', 1)[0], 'type', name);
    }
    const access = nodes.slice().reverse().find(node =>
        [scripts.node_types.MemberAccess, scripts.node_types.NamespaceAccess].includes(node.type)
        && node.children[1]?.start <= relative && relative <= node.children[1]?.end);
    const call = nodes.find(node => node.type == scripts.node_types.FunctionCall && node.children[0]?.end == token.end);
    const kind = call ? 'function' : 'property';
    if (access?.type == scripts.node_types.MemberAccess) {
        const owner = nativeOwner(expressionType(access.children[0]));
        return owner ? candidate(owner, kind) : null;
    }
    if (access?.type == scripts.node_types.NamespaceAccess) {
        const namespace = module.content.substring(statement.start_offset + access.children[0].start,
            statement.start_offset + access.children[0].end).replace(/\s/g, '').replace(/^::/, '');
        return { ...candidate(namespace, kind), alternatives: [candidate('__' + namespace, kind)] };
    }
    if (nodes.some(node => node.type == scripts.node_types.NamespaceAccess && node.children[0]?.end == token.end))
        return candidate(token.value, 'type');
    if (call) {
        const global = candidate('__' + scope.getNamespace().getQualifiedNamespace(), 'function');
        const owner = nativeOwner(scope.getParentType()?.name);
        return owner ? { ...candidate(owner, 'function'), alternatives: [global, candidate(name, 'type')] }
            : { ...global, alternatives: [candidate(name, 'type')] };
    }
    return null;
}
