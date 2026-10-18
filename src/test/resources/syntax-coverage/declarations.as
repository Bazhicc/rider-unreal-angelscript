// Declarations, literals, lexical scopes, type deduction and default initialization.
local int TestLocalSyntax()
{
	return 6;
}

int TestSyntax()
{
	int Empty = 0;
	bool Disabled = false;
	FString Text;
	UObject Object;
	if (Empty != 0 || Disabled || !Text.IsEmpty() || Object != nullptr)
	{
		return 1;
	}
	const int Hex = 0x2A;
	const uint Bits = 0b101010;
	auto Deduced = Hex;
	int First = 3, Second = First + 4;
	int Total = First + Second;
	{
		int Scoped = 5;
		Total += Scoped;
	}
	int Expected = 42;
	if (Hex != Expected || Bits != uint(Expected) || Deduced != 42 || Total != 15)
	{
		return 2;
	}
	int8 Signed = -12;
	uint8 Unsigned = 250;
	int16 Short = -1234;
	uint16 Wide = 60000;
	int64 Large = 5000000001;
	uint64 Larger = 9000000001;
	float32 Single = 1.25f;
	float64 Double = 2.5;
	if (Signed != -12 || Unsigned != 250 || Short != -1234 || Wide != 60000)
	{
		return 3;
	}
	if (Large != 5000000001 || Larger != 9000000001 || Single != 1.25f || Double != 2.5)
	{
		return 4;
	}
	int Letter = int('A'), Newline = int('\n'), Tab = int('\t');
	if (Letter != 65 || Newline != 10 || Tab != 9)
	{
		return 5;
	}
	float FloatAlias = 1.25f;
	double DoubleAlias = 2.5;
	int32 IntAlias = 3;
	uint32 UIntAlias = 4;
	if (FloatAlias != 1.25f || DoubleAlias != 2.5 || IntAlias + UIntAlias != 7 || TestLocalSyntax() != 6)
	{
		return 6;
	}
	return 0;
}
