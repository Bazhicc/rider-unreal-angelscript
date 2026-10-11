struct FOverloadValue { int Value; }
struct FNonPOD { int Value; FString Text; }
class AActorFixture : AActor
{
    TMap<FOverloadValue, FNonPOD> DynamicScoresByKey;
    int AddScore(int Amount) { return Amount; }
    int NativeCallProbe()
    {
        return UAtocNativeCallFixture::Invoke(this, n"AddScore", AddScore(1), AddScore(2));
    }
    void ReplaceDynamicScores()
    {
        for (auto It : DynamicScoresByKey)
        {
            if (It.GetKey().Value == 2)
            {
                It.RemoveCurrent();
                continue;
            }
            FNonPOD Replacement = It.GetValue();
            Replacement.Value += 100;
            Replacement.Text = "Replaced";
            It.SetValue(Replacement);
            auto Copy = It;
            Copy.GetValue().Value += 1;
        }
    }
}
AActorFixture SpawnFixtureActor()
{
    return SpawnActor(AActorFixture);
}
void MoreNativeForms(AActorFixture Actor, TArray<FNonPOD> Items, TMap<int, TArray<FNonPOD>> Nested)
{
    auto Spawned = SpawnActor(AActorFixture);
    Spawned.AddScore(3);
    auto NamedSpawned = SpawnActor(Class: AActorFixture);
    NamedSpawned.AddScore(4);
    SpawnActor(Class: AActorFixture).AddScore(5);
    auto ReorderedSpawned = SpawnActor(bDeferredSpawn: true, Class: AActorFixture);
    ReorderedSpawned.AddScore(6);
    AActor Parent = Actor;
    Parent.GetActorLocation().X += 1;
    auto Iterator = Actor.DynamicScoresByKey.Iterator();
    Iterator.GetValue().Text = "Changed";
    for (auto Value : Items) { Value.Value += 1; }
    for (auto Outer : Nested) { for (auto Value : Outer.GetValue()) { Value.Text = "Nested"; } }
    auto Mode = EFixtureMode::Second;
    auto Length = MathFixture::Length(1.0);
    auto Now = GlobalClock();
    auto Name = FName("Name");
    auto Hidden = Actor.ActorHiddenInGame;
    Actor.Health = 7;
    Actor.SharedNativeName();
    UAtocNativeCallFixture::LaterAdded(Actor);
    Iterator.LaterAdded();
}
// A name-only search must not select these unrelated members.
class WrongOwner { void SharedNativeName() {} int Health; }
int GlobalClock(int Unrelated) { return Unrelated; }

void ContainerNavigation(TMap<int, FName> Map, TSet<int> Set)
{
    Map.Add(1, n"One");
    Map.Contains(1);
    Map.Remove(1);
    Map.FindOrAdd(2);
    Map.Empty();
    Map.Num();
    Set.Add(1);
    Set.Contains(1);
    Set.Remove(1);
    Set.Empty();
    Set.Num();
    auto SetIt = Set.Iterator();
    SetIt.Proceed();
}
