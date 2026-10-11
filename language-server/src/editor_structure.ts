import { FoldingRange, Position, SelectionRange } from 'vscode-languageserver';
import { ASModule, ASScope } from './as_parser';

// Reuse the official parser's scopes and AST offsets, including UTF-16 document positions.
export function foldingRanges(module: ASModule): FoldingRange[] {
    const ranges: FoldingRange[] = [];
    function visit(scope: ASScope) {
        if (scope !== module.rootscope) {
            const start = module.getPosition(scope.start_offset);
            const end = module.getPosition(scope.end_offset);
            // Leave a closing brace on its own line visible.
            const prefix = module.content.slice(module.textDocument.offsetAt({ line: end.line, character: 0 }), scope.end_offset);
            const endLine = prefix.trim() === '' ? end.line - 1 : end.line;
            if (endLine > start.line) ranges.push({ startLine: start.line, endLine });
        }
        for (const child of scope.scopes) visit(child);
    }
    if (module.rootscope) visit(module.rootscope);
    return ranges;
}

export function selectionRanges(module: ASModule, positions: Position[]): SelectionRange[] {
    return positions.map(position => {
        const offset = module.textDocument.offsetAt(position);
        const spans: [number, number][] = [[0, module.content.length]];
        function add(start: number, end: number) {
            if (start <= offset && offset <= end && start >= 0 && end <= module.content.length && end > start)
                spans.push([start, end]);
        }
        function nodeSpans(node: any, base: number) {
            if (!node) return;
            if (typeof node.start === 'number' && typeof node.end === 'number') add(base + node.start, base + node.end);
            if (node.children) for (const child of node.children) nodeSpans(child, base);
        }
        function visit(scope: ASScope) {
            if (scope.start_offset > offset || scope.end_offset < offset) return;
            add(scope.start_offset, scope.end_offset);
            if (scope.previous) add(scope.previous.start_offset, Math.min(scope.end_offset + 1, module.content.length));
            for (const statement of scope.statements) {
                if (!statement || statement.start_offset > offset || statement.end_offset < offset) continue;
                add(statement.start_offset, statement.end_offset);
                nodeSpans(statement.ast, statement.start_offset);
            }
            for (const child of scope.scopes) visit(child);
        }
        if (module.rootscope) visit(module.rootscope);
        // An identifier is a useful first selection even while its surrounding statement is incomplete.
        let start = offset, end = offset;
        const identifier = /[\p{L}\p{N}_]/u;
        while (start > 0 && identifier.test(module.content[start - 1])) --start;
        while (end < module.content.length && identifier.test(module.content[end])) ++end;
        add(start, end);
        spans.sort((a, b) => (b[1] - b[0]) - (a[1] - a[0]));
        let parent: SelectionRange = { range: module.getRange(0, module.content.length) };
        let previous: [number, number] = [0, module.content.length];
        for (const span of spans) {
            if (span[0] === previous[0] && span[1] === previous[1]) continue;
            if (span[0] < previous[0] || span[1] > previous[1]) continue;
            parent = { range: module.getRange(span[0], span[1]), parent };
            previous = span;
        }
        return parent;
    });
}
