// Complete-input checks complement the historical token/tree snapshots.
namespace Coverage
{
    enum EMode { First, Second = 3 }
    typedef int Number;
    interface IWorker { void Work(); }
    struct FData
    {
        int Value;
        FData() { Value = 0; }
        FData(int InValue) { Value = InValue; }
        ~FData() {}
        int Alias { get const { return Value; } set { Value = value; } }
        int GetNumber() const property { return Value; }
        void SetNumber(int InValue) property { Value = InValue; }
    }
    UCLASS(Abstract, meta = (DisplayName = "Worker"))
    class AWorker : AActor
    {
        UPROPERTY(EditAnywhere)
        int Count = 0;
        UFUNCTION(BlueprintOverride)
        void Tick(float DeltaSeconds) { Count += 1; }
        private int Read() const override final { return Super::Read(); }
        default Count = 3;
    }
    event void OnChanged(int Value);
    delegate void FCallback(int Value);
    mixin void Extra(FData& Data, int Value = 1) { Data.Value += Value; }
    local int Helper(int Value = 3) { return Value; }
    asset WorkerAsset of AWorker { Count = 4; }
    access Internal = private;
    class FRestricted
    {
        access MemberInternal = private;
        access:MemberInternal int Secret;
        access:Internal int Value;
        access:Internal void Reset() { Value = 0; }
    }

    int& Reference(int& inout Value) { return Value; }
    void Containers(const TArray<FData>& in Values, int& out Result)
    {
        TMap<int, TArray<TMap<int, FString>>> Nested;
        TArray<int> Numbers = {1, 2, 3};
        FData Constructed(3);
        auto Deduced = Numbers[0];
        Result = Helper(Value: Deduced);
        Result = Helper(Value = Result);
        for (int Index = 0; Index < 3; ++Index) { Result += Numbers[Index]; }
        for (; Result > 0; --Result) { if (Result == 2) continue; }
        while (Result < 2) { Result++; }
        do { --Result; } while (Result > 0);
        switch (Result) { case 0: Result = 1; break; default: Result = 2; }
        bool Flag = true and not false or false xor true;
        Flag = (Result > 0) ^^ (Result < 3);
        Result = (Flag ? Result & 7 | 2 ^ 1 : (Result, 2));
        Result = (Result << 1) + (Result >> 1) + (Result >>> 1);
        Result += 1; Result -= 1; Result *= 2; Result /= 2; Result %= 3;
        Result &= 7; Result |= 1; Result ^= 2;
        Result <<= 1; Result >>= 1; Result >>>= 1; Result **= 2;
        Result = cast<int>(3.0);
        UObject Object = nullptr;
        Flag = Object is nullptr;
        Flag = Object !is nullptr;
        FString Text = "hello\n" 'world';
        FName Name = n"Name";
        FText Label = t"Label";
        FString Raw = r"raw\path";
        FString Formatted = f"value {Helper(Value: Flag ? 1 : 2):04d} {{escaped}}";
    }
}
