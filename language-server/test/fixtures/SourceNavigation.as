class AOfflineActor : AActor
{
    TMap<int, int> Values;
    void MoveOther(AActor Other, FVector Location)
    {
        Other.SetActorLocation(Location);
        Location.X += 1;
        Values.Add(1, 2);
        GetActorLocation();
        UAtocNativeCallFixture::Invoke(this, n"Callback", 1, 2);
        SpawnActor(AOfflineActor);
    }
}
void UseEnum(ETeleportType Mode) {}
void UnresolvedReceiver() { Unknown.GetActorLocation(); }
