int DefaultTarget(int First = 1, int Second = 2, int Third = 3) { return First; }
int DefaultNamed() { return DefaultTarget(Third: 7, First: 19); }