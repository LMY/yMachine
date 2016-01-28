package y;

public enum Op {
	NOP,
	
	INC,
	DEC,
	NOT,
	
	MOV,
	ADD,
	SUB,
	MUL,
	DIV,
	MOD,
	
	AND,
	OR,
	XOR,
	
	SHR,
	SHL,
	ROR,
	ROL,
	
	TEST,
	JMP;
	
	public static final String[] names = {  "NOP",
		"INC", "DEC", "NOT",
		"MOV", "ADD", "SUB", "MUL", "DIV", "MOD", "AND", "OR", "XOR", "SHR", "SHL", "ROR", "ROL", "TEST", "JMP", };
	
	public String getName() { return names[ordinal()]; }
	
	public static Op create(byte ordinal) { return Op.values()[ordinal]; }
	public static Op create(int ordinal) { return Op.values()[ordinal]; }
	
	public byte getCode() { return (byte) ordinal(); }

	public static Op create(String s)
	{
		for (int i=1; i<names.length; i++)
			if (s.equalsIgnoreCase(names[i]))
				return create(i);
		return NOP;
	}
}
