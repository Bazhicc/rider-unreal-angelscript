using System.Reflection;
using System.Runtime.CompilerServices;
using System.Runtime.Loader;
using System.Text.Json;
using JetBrains.Application.Progress;
using JetBrains.DocumentModel;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Modules;
using JetBrains.ReSharper.Psi.Search;
using JetBrains.ReSharper.Psi.Tree;
using JetBrains.Util.DataStructures;
using ReSharperPlugin.UnrealAngelscript;

internal static class Program
{
    public static void Main(string[] args)
    {
        AssemblyLoadContext.Default.Resolving += (context, name) => {
            var file = Path.Combine(args[0], name.Name + ".dll");
            return File.Exists(file) ? context.LoadFromAssemblyPath(file) : null;
        };
        if (args.Length == 3) Transport(args[1], args[2]); else Run();
    }

    [MethodImpl(MethodImplOptions.NoInlining)]
    private static void Transport(string url, string token)
    {
        var ranges = new BridgeEndpoint(url, token).Find(new NativeUsageQuery("NativeCall",
            [new NativeDeclaration("F:/Native 中文/Probe.h", 17, 1, 4)]));
        Console.WriteLine(JsonSerializer.Serialize(new BridgeResponse(ranges), new JsonSerializerOptions(JsonSerializerDefaults.Web)));
    }

    [MethodImpl(MethodImplOptions.NoInlining)]
    private static void Run()
    {
        var content = "// 中文 😀\r\nvoid Call();\r\n";
        var document = Stub<IDocument>.Create((method, args) => {
            if (method == "GetOffsetByCoordsSafe") {
                var coords = (DocumentCoords)args![0]!;
                var lines = content.Split('\n');
                var line = (int)coords.Line;
                var column = (int)coords.Column;
                return line < lines.Length && column <= lines[line].TrimEnd('\r').Length
                    ? lines.Take(line).Sum(value => value.Length + 1) + column : (int?)null;
            }
            throw new Exception("Unexpected document access: " + method);
        });
        var source = Stub<IPsiSourceFile>.Create((method, _) => method == "get_Document" ? document : throw new Exception(method));
        var excluded = Stub<IPsiSourceFile>.Create((method, _) => method == "get_Document" ? document : throw new Exception(method));
        var range = new UsageRange("F:/Script 中文/Probe.as", 0, 1, 1, 5, 1, 9);
        var query = new NativeUsageQuery("NativeCall", [new NativeDeclaration("F:/Native.h", 17, 1, 4)]);
        var calls = 0;
        var searcher = new NativeUsageSearcher([query], actual => {
            Require(actual == query, "Preserve the native declaration identity");
            ++calls;
            return [(source, range), (source, range), (excluded, range)];
        });
        var found = new List<FindResult>();
        var consumer = new FindResultConsumer<FindResult>(result => result, result => { found.Add(result); return FindExecution.Continue; });
        Require(!searcher.ProcessProjectItem(source, consumer) && found.Count == 0, "Initial phase must preserve other native searchers");
        var phase = searcher.TryCreateSecondPhaseInfo(new Domains(), new Domain([source]), []);
        Require(phase.Searcher == searcher && phase.SearchDomain is Domain { IsEmpty: false }, "Build the filtered second search phase");
        Require(searcher.ProcessProjectItem(source, consumer), "Emit AS results through the native consumer");
        Require(!searcher.ProcessProjectItem(excluded, consumer), "Exclude out-of-scope script files");
        Require(found.Count == 1, "Deduplicate AS references");
        var actualRange = ((FindResultText)found.Single()).DocumentRange.TextRange;
        Require(actualRange.StartOffset == content.IndexOf("Call", StringComparison.Ordinal) && actualRange.Length == 4,
            "Map UTF-16 positions using backend CRLF document coordinates");
        Require(calls == 1, "Query each target once in the second phase");
        var empty = new NativeUsageSearcher([query], _ => [(source, range)]).TryCreateSecondPhaseInfo(new Domains(), new Domain([]), []);
        Require(empty.Searcher is null && empty.SearchDomain is null, "An empty scope must not add results");
        Require(new BridgeEndpoint("https://example.org/references", "test").Find(query).Length == 0,
            "Reject non-local endpoints before issuing a request");
        Console.WriteLine("BACKEND_NATIVE_USAGES passed: phase integration, scopes, deduplication, UTF-16/CRLF, declaration identity, local transport");
    }

    private static void Require(bool condition, string message) { if (!condition) throw new Exception(message); }
}

public class Stub<T> : DispatchProxy where T : class
{
    private Func<string, object?[]?, object?> callback = null!;
    public static T Create(Func<string, object?[]?, object?> callback) {
        var proxy = Create<T, Stub<T>>();
        ((Stub<T>)(object)proxy).callback = callback;
        return proxy;
    }
    protected override object? Invoke(MethodInfo? targetMethod, object?[]? args) => callback(targetMethod!.Name, args);
}

internal sealed class Domain(IEnumerable<IPsiSourceFile> files) : ISearchDomain
{
    internal readonly HashSet<IPsiSourceFile> Files = files.ToHashSet();
    public bool IsEmpty => Files.Count == 0;
    public bool HasPhysicalPart => true;
    public ISearchDomain Intersect(ISearchDomain other) => new Domain(Files.Intersect(((Domain)other).Files));
    public ISearchDomain Union(ISearchDomain other) => new Domain(Files.Union(((Domain)other).Files));
    public bool Contains(ISearchDomain other) => ((Domain)other).Files.IsSubsetOf(Files);
    public bool HasIntersectionWith(IDeclaredElement element) => false;
    public void Accept(SearchDomainVisitor visitor) => throw new NotSupportedException();
    public void Dump(TextWriter writer) { }
    public ISearchDomain ExtractLibraryDomain() => new Domain([]);
    public ISearchDomain ExtractNonPhysicalDomain() => new Domain([]);
    public bool IsValid() => true;
    public bool Equals(ISearchDomain? other) => other is Domain domain && Files.SetEquals(domain.Files);
}

internal sealed class Domains : SearchDomainFactory
{
    public override ISearchDomain CreateSearchDomain(IPsiSourceFile file) => new Domain([file]);
    public override ISearchDomain CreateSearchDomain(IEnumerable<IPsiSourceFile> files) => new Domain(files);
    public override ISearchDomain CreateSearchDomain(HybridCollection<IPsiSourceFile> files) => throw new NotSupportedException();
    public override ISearchDomain CreateSearchDomain(IProjectModelElement element) => throw new NotSupportedException();
    public override ISearchDomain CreateSearchDomain(IPsiModule module) => throw new NotSupportedException();
    public override ISearchDomain CreateSearchDomain(IEnumerable<IPsiModule> modules) => throw new NotSupportedException();
    public override ISearchDomain CreateSearchDomain(ISolution solution, bool includeLibraries) => throw new NotSupportedException();
    public override ISearchDomain CreateSearchDomain(ITreeNode element) => throw new NotSupportedException();
    public override ISearchDomain CreateSearchDomain(IEnumerable<ITreeNode> elements) => throw new NotSupportedException();
    public override ISearchDomain CreateLibrariesDomain(ISolution solution) => throw new NotSupportedException();
    public override ISearchDomain CreatePublicAccessibilityDomain(IPsiModule module) => throw new NotSupportedException();
    public override ISearchDomain CreatePublicAccessibilityDomain(IEnumerable<IPsiModule> modules) => throw new NotSupportedException();
    public override ISearchDomain CreateInternalAccessibilityDomain(IPsiModule module) => throw new NotSupportedException();
}
