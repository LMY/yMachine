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
	
	public static byte[] compile(String text) throws Exception {
		final String[] lines = text.split("\n");
		
		final List<Byte> compiled = new ArrayList<Byte>();
		for (int linen=0; linen<lines.length; linen++) {
			final String line = lines[linen].trim();
			if (line.isEmpty() || line.startsWith(";") || line.startsWith("#") || line.startsWith("//"))
				continue;
			
			final List<Byte> onelinecompiled = compileLine(line, linen);
			if (onelinecompiled != null)
				compiled.addAll(onelinecompiled);
		}
		
		final byte[] ret = new byte[compiled.size()];
		for (int i=0; i<ret.length; i++)
			ret[i] = compiled.get(i);
		return ret;		
	}
	
	public static List<Byte> compileLine(String line, int linen) throws Exception {
		
		String[] args = line.split("[\\s,]");

		if (args[0].startsWith("J") && args.length == 2) {
			
			final String orig = args[0];
			args = new String[] { "JMP", "", args[1] }; 
			
			int reg = 0;
			boolean negate = false;
			
			for (int i=1; i<orig.length(); i++) {
				switch (orig.charAt(i)) {
				case 'N': negate = true; break;
				case 'Z': reg |= (negate?16:1);  negate = false; break;
				case 'E': reg |= (negate?32:2);  negate = false; break;
				case 'G': reg |= (negate?64:4);  negate = false; break;
				case 'L': reg |= (negate?128:8);  negate = false; break;
				default : throw new Exception(""+(linen+1)+" ERROR: Invalid jump number ("+orig.charAt(i)+")");
				}
			}
			args[1] = ""+reg;
		}
		
		final List<Byte> ret = new ArrayList<Byte>();
		
		final Op op = Op.create(args[0]);
		
		// check number of arguments
		if ((op == NOP && args.length != 1) ||
			(op == INC && args.length != 2) || (op == DEC && args.length != 2) || (op == NOT && args.length != 2) ||		

			(op == MOV && args.length != 3) || (op == ADD && args.length != 3) || (op == SUB && args.length != 3) ||		
			(op == MUL && args.length != 3) || (op == DIV && args.length != 3) || (op == MOD && args.length != 3) ||		
			(op == AND && args.length != 3) || (op == OR && args.length != 3) || (op == XOR && args.length != 3) ||		
			(op == SHR && args.length != 3) || (op == SHL && args.length != 3) || (op == ROR && args.length != 3) ||		
			(op == ROL && args.length != 3) || (op == TEST && args.length != 3) || (op == JMP && args.length != 3))
			throw new Exception(""+(linen+1)+" ERROR: Invalid arg number ("+args.length+")");
			
		ret.add(op.getCode());
		
		for (int i=1; i<args.length; i++) {
			List<Byte> compiledLine = compileValue(args[i]);
			
//			if (i == 1 && compiledLine.size() == 8) {
//				System.out.println(""+(linen+1)+" WARNING: First argument must be a register");
//				
//				final List<Byte> newcompiledLine = new ArrayList<Byte>();
//				newcompiledLine.add(compiledLine.get(4));
//				newcompiledLine.add(compiledLine.get(5));
//				newcompiledLine.add(compiledLine.get(6));
//				newcompiledLine.add(compiledLine.get(7));
//				compiledLine = newcompiledLine;
//			}
//			else if (i == 1 && compiledLine.size() > 8)
//				throw new Exception(""+(linen+1)+" ERROR: First argument must be a register");
			
			ret.addAll(compiledLine);
		}
		
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
