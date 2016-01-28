package y;

import java.util.ArrayList;
import java.util.List;


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
	
	public static byte[] compile(String text) {
		final String[] lines = text.split("\n");
		
		final List<Byte> compiled = new ArrayList<Byte>();
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith(";") || line.startsWith("#") || line.startsWith("//"))
				continue;
			
			final List<Byte> onelinecompiled = compileLine(line);
			if (onelinecompiled != null)
				compiled.addAll(onelinecompiled);
		}
		
		final byte[] ret = new byte[compiled.size()];
		for (int i=0; i<ret.length; i++)
			ret[i] = compiled.get(i);
		return ret;		
	}
	
	public static List<Byte> compileLine(String line) {
		
		final String[] args = line.split("\\s");
		
		final List<Byte> ret = new ArrayList<Byte>();
		
		final Op op = Op.create(args[0]);		
		ret.add(op.getCode());
		
		for (int i=1; i<args.length; i++)
			ret.addAll(compileValue(args[i]));
		
		return ret;
	}
	
	public static final int REF_INT_VALUE = 0xFFFFFFFF;
	public static final byte[] REF_VALUE = toByteArray(REF_INT_VALUE);

	public static int fromByteArray(byte[] bytes) {
	     return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}
	public static byte[] toByteArray(int value) {
	    return new byte[] { (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value };
	}
	
	
	public static List<Byte> compileValue(String line) {
		
		try {
			final List<Byte> ret = new ArrayList<Byte>();
			
			while (line.startsWith("#")) {
				line = line.substring(1);
				
				ret.add(REF_VALUE[0]);
				ret.add(REF_VALUE[1]);
				ret.add(REF_VALUE[2]);
				ret.add(REF_VALUE[3]);
			}
			final int x = Integer.parseInt(line);
			ret.add((byte)(x/256/256/256));
			ret.add((byte)(x/256/256));
			ret.add((byte)(x/256));
			ret.add((byte)x);

			return ret;
		}
		catch (Exception e) { return new ArrayList<Byte>(); }
	}
}
