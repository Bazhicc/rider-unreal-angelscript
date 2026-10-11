class AOfflineActor : AActor
{
    TMap<int, int> Values;
    TSet<int> UniqueValues;
    TMap<FName, FVector> Points;
    TArray<FVector> Vectors;
    TSet<FVector> UniqueVectors;
    FString StringValue;
    void MoveOther(AActor Other, FVector Location)
    {
        Other.SetActorLocation(Location);
        Location.X += 1;
        Values.Add(1, 2);
        Values.FindOrAdd(1);
        Values.Contains(1);
        Values.Remove(1);
        Values.Empty();
        Values.Num();
        UniqueValues.Add(1);
        UniqueValues.Contains(1);
        UniqueValues.Remove(1);
        UniqueValues.Empty();
        UniqueValues.Num();
        GetActorLocation();
        UAtocNativeCallFixture::Invoke(this, n"Callback", 1, 2);
        SpawnActor(AOfflineActor);
    }
    void IterateOffline()
    {
        for (auto MapIt : Points)
        {
            MapIt.GetKey();
            FVector Value = MapIt.GetValue();
            MapIt.SetValue(Value);
            MapIt.RemoveCurrent();
            MapIt.GetValue().X += 1;
        }
        auto ExplicitIt = Points.Iterator();
        ExplicitIt.GetKey();
        auto AliasIt = ExplicitIt;
        AliasIt.GetValue();
        for (auto Vector : Vectors) Vector.X += 1;
        for (auto Vector : UniqueVectors) Vector.X += 1;
    }
}
void UseEnum(ETeleportType Mode) {}
void UnresolvedReceiver() { Unknown.GetActorLocation(); }
