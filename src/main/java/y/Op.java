package y;

public enum Op {
	NOP,
	
	INC,
	DEC,
	NOT,
	
	DATA,
	PRINTCHAR,
	PRINTINT,
	PRINTSTRING,
	READ,
	
	CLONE,
	FORK,
	START,
	JOIN,
	KILL,
	FREE,
	
	LOADCODE,
	IN,
	OUT,
	
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
	JMP,
	
	ALLOC,
	RETURN;
	
	public static final String[] names = {  "NOP",
		"INC", "DEC", "NOT",
		"DATA", "PRINTCHAR", "PRINTINT", "PRINTSTRING", "READ",
		"CLONE", "FORK", "START", "JOIN", "KILL", "FREE", "LOADCODE", "IN", "OUT",
		"MOV", "ADD", "SUB", "MUL", "DIV", "MOD", "AND", "OR", "XOR", "SHR", "SHL", "ROR", "ROL", "TEST", "JMP", "ALLOC", "RETURN" };
	
	public String getName() { return names[ordinal()]; }
	
	public static Op create(byte ordinal) { return Op.values()[ordinal]; }
	public static Op create(int ordinal) { return Op.values()[ordinal]; }
	
	public byte getCode() { return (byte) ordinal(); }

	public static Op create(String s) throws Exception
	{
		for (int i=0; i<names.length; i++)
			if (s.equalsIgnoreCase(names[i]))
				return create(i);

		throw new Exception("Invalid opcode '"+s+"'");
	}
}
