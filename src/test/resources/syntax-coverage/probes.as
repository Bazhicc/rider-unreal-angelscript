#ifdef ATOC_TEST_PROBES
int ProbeCount = 0;
#ifndef DISABLE_STRING_PROBE
FString QuotedDefault(const FString& Value = "A\"B\\C\nD")
{
    return Value;
}
FString GroupedQuote(const FString& Value = (("中文)\"(")))
{
    return Value;
}
FString SingleQuote(const FString& Value = 'A\'B\\C')
{
    return Value;
}
bool CompareEscaped()
{
    return QuotedDefault() == "Quote:\"X\" Path:C:\\Temp\n中文\t\\n";
}
FName NamedQuote(const FName& Value = n"Name\"Suffix") { return Value; }
FText TextQuote(const FText& Value = t"Text\"Suffix") { return Value; }
FString RawQuote(const FString& Value = """C:\Temp\n"quoted"中文""") { return Value; }
FString FormatQuote() { return f"{QuotedDefault(Value: "A\"B"):04d}"; }
#elif OTHER_STRING_PROBE
int AlternateProbe() { return 2; }
#else
int AlternateProbe() { return 3; }
#endif
#endif
int AfterProbes() { return 4; }
