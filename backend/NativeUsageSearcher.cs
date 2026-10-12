using System.Net.Http.Json;
using System.Text.Json;
using JetBrains.Application;
using JetBrains.Diagnostics;
using JetBrains.Util.Logging;
using JetBrains.Util.dataStructures.TypedIntrinsics;
using JetBrains.DocumentManagers;
using JetBrains.Application.Parts;
using JetBrains.DocumentModel;
using JetBrains.ProjectModel;
using JetBrains.ReSharper.Psi;
using JetBrains.ReSharper.Psi.Cpp.Language;
using JetBrains.ReSharper.Psi.ExtensionsAPI;
using JetBrains.ReSharper.Psi.Search;
using JetBrains.ReSharper.Psi.Tree;
using JetBrains.Util;

namespace ReSharperPlugin.UnrealAngelscript;

// Adds AS results to the existing native search, including Show Usages. No additional target/action.
[PsiComponent(Instantiation.DemandAnyThreadSafe)]
public sealed class NativeUsageSearcherFactory(ISolution solution, DocumentManager documents) : DomainSpecificSearcherFactoryBase
{
    public override bool IsCompatibleWithLanguage(PsiLanguageType language) => language is CppLanguage;

    public override IEnumerable<ITwoPhaseDomainSpecificSearcher> CreateImplicitUsageSearchers(
        SearchPattern pattern, ICollection<IDeclaredElement> elements)
    {
        var native = elements.Where(e => e.PresentationLanguage is CppLanguage).ToArray();
        var endpoint = BridgeEndpoint.Read(solution.SolutionDirectory.FullPath);
        if (native.Length == 0 || endpoint is null) return [];
        var queries = native.Select(element => new NativeUsageQuery(element.ShortName,
            element.GetDeclarations().Select(declaration => {
                var range = declaration.GetContainingFile().GetDocumentRange(declaration.GetNameRange());
                var position = new DocumentOffset(range.Document, range.TextRange.StartOffset).ToDocumentCoords();
                return new NativeDeclaration(declaration.GetSourceFile().GetLocation().FullPath, range.TextRange.StartOffset,
                    (int)position.Line, (int)position.Column);
            }).Distinct().ToArray())).ToArray();
        return [new NativeUsageSearcher(queries, query => endpoint.Find(query).Select(range => {
            var document = documents.GetOrCreateDocument(VirtualFileSystemPath.Parse(range.Path, InteractionContext.SolutionContext));
            return ((IPsiSourceFile?)document.GetPsiSourceFile(solution), range);
        }))];
    }

    public override ISearchDomain GetDeclaredElementSearchDomain(IDeclaredElement element)
    {
        if (element.PresentationLanguage is not CppLanguage) return base.GetDeclaredElementSearchDomain(element);
        var psi = element.GetPsiServices();
        // C++ accessibility domains otherwise omit script/miscellaneous modules.
        return psi.SearchDomainFactory.CreateSearchDomain(psi.Modules.GetSourceModules().SelectMany(module => module.SourceFiles)
            .Where(file => file.Name.EndsWith(".as", StringComparison.OrdinalIgnoreCase)));
    }
}

internal sealed class NativeUsageSearcher(NativeUsageQuery[] queries,
    Func<NativeUsageQuery, IEnumerable<(IPsiSourceFile? Source, UsageRange Range)>> fetch) : ITwoPhaseDomainSpecificSearcher
{
    private Dictionary<IPsiSourceFile, List<UsageRange>> usages = new();
    public bool ProcessProjectItem<TResult>(IPsiSourceFile sourceFile, IFindResultConsumer<TResult> consumer)
    {
        if (!usages.TryGetValue(sourceFile, out var ranges)) return false;
        foreach (var range in ranges)
        {
            var document = sourceFile.Document;
            var start = document.GetOffsetByCoordsSafe(new DocumentCoords((Int32<DocLine>)range.StartLine, (Int32<DocColumn>)range.StartCharacter));
            var end = document.GetOffsetByCoordsSafe(new DocumentCoords((Int32<DocLine>)range.EndLine, (Int32<DocColumn>)range.EndCharacter));
            if (!start.HasValue || !end.HasValue || end <= start) continue;
            var result = consumer.Build(new FindResultText(sourceFile,
                new DocumentRange(document, new TextRange(start.Value, end.Value))));
            if (result is not null && consumer.Merge(result) == FindExecution.Stop) return true;
        }
        return true;
    }

    public bool ProcessElement<TResult>(ITreeNode element, IFindResultConsumer<TResult> consumer) => false;

    public (IDomainSpecificSearcher? Searcher, ISearchDomain? SearchDomain) TryCreateSecondPhaseInfo(
        SearchDomainFactory factory, ISearchDomain searchDomain, HashSet<IPsiSourceFile>? alreadyProcessed)
    {
        foreach (var query in queries)
        {
            foreach (var (source, range) in fetch(query))
            {
                if (source is null) { Logger.GetLogger<NativeUsageSearcher>().Warn("AS file has no native project source: " + range.Path); continue; }
                if (factory.CreateSearchDomain(source).Intersect(searchDomain).IsEmpty) continue;
                if (!usages.TryGetValue(source, out var ranges)) usages[source] = ranges = [];
                if (!ranges.Contains(range)) ranges.Add(range);
            }
        }
        return usages.Count == 0 ? (null, null) : (this, factory.CreateSearchDomain(usages.Keys).Intersect(searchDomain));
    }
}

internal sealed record NativeDeclaration(string Path, int Offset, int Line, int Character);
internal sealed record NativeUsageQuery(string CppName, NativeDeclaration[] Declarations);
internal sealed record UsageRange(string Path, int Start, int End, int StartLine, int StartCharacter, int EndLine, int EndCharacter);
internal sealed record BridgeResponse(UsageRange[] Usages);
internal sealed record BridgeEndpoint(string Url, string Token)
{
    private static readonly JsonSerializerOptions Json = new(JsonSerializerDefaults.Web);
    public static BridgeEndpoint? Read(string root)
    {
        var path = System.IO.Path.Combine(root, ".idea", "angelscript-native-bridge.json");
        try { return JsonSerializer.Deserialize<BridgeEndpoint>(File.ReadAllText(path), Json); }
        catch (IOException) { return null; }
        catch (JsonException) { return null; }
    }

    public UsageRange[] Find(NativeUsageQuery query)
    {
        // Both processes belong to this Rider instance. The random bearer token protects the local endpoint.
        if (!Uri.TryCreate(Url, UriKind.Absolute, out var uri) || uri.Host != "127.0.0.1" || uri.Scheme != "http") return [];
        using var client = new HttpClient { Timeout = TimeSpan.FromSeconds(30) };
        using var cancellation = new CancellationTokenSource();
        client.DefaultRequestHeaders.Add("Authorization", "Bearer " + Token);
        try
        {
            var request = client.PostAsJsonAsync(uri, query, Json, cancellation.Token);
            while (!request.IsCompleted) {
                Interruption.Current.CheckAndThrow();
                Thread.Sleep(50);
            }
            using var response = request.GetAwaiter().GetResult();
            response.EnsureSuccessStatusCode();
            return response.Content.ReadFromJsonAsync<BridgeResponse>(Json).GetAwaiter().GetResult()?.Usages ?? [];
        }
        catch (HttpRequestException exception) { Logger.GetLogger<BridgeEndpoint>().Warn(exception, "AngelScript native usage bridge failed"); return []; }
        catch (TaskCanceledException exception) { Logger.GetLogger<BridgeEndpoint>().Warn(exception, "AngelScript native usage bridge timed out"); return []; }
        finally { cancellation.Cancel(); }
    }
}
