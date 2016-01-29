package y;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compiler {
	
	private Map<String,Integer> labels;
	
	public Compiler() {
		labels = new HashMap<String,Integer>();
	}
	
	public static byte[] compile(String text) throws Exception {
		final Compiler compiler = new Compiler();
		return compiler.compileLines(text);
	}
	
	public byte[] compileLines(String text) throws Exception {
		final String[] lines = text.split("\n");
		
		final List<Byte> compiled = new ArrayList<Byte>();
		for (int linen=0; linen<lines.length; linen++) {
			final String line = lines[linen].trim();
			if (line.isEmpty() || line.startsWith(";") || line.startsWith("#") || line.startsWith("//"))
				continue;
			
			final List<Byte> onelinecompiled = compileLine(line, linen, compiled.size());
			if (onelinecompiled != null)
				compiled.addAll(onelinecompiled);
		}
		
		final byte[] ret = new byte[compiled.size()];
		for (int i=0; i<ret.length; i++)
			ret[i] = compiled.get(i);
		return ret;		
	}
	
	public List<Byte> compileLine(String line, int linen, int addr) throws Exception {
		
		// save labels
		if (line.contains(":")) {
			final String[] p = line.split(":");
			if (p.length != 2)
				throw new Exception(""+(linen+1)+" ERROR: Multiple labels (or usage of ':'s)");
			line = p[1].trim();

			// commented = labels can be redefined
//			if (labels.get(p[0]) != null)
//				throw new Exception(""+(linen+1)+" ERROR: Label '"+p[0]+"' already defined at "+labels.get(p[0]));
			
			labels.put(p[0].trim(), addr);
		}
		
		String[] args = Utils.splitAndTrim(line, "[\\s,]");

		// decode conditional JMPs
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
		
		if (args[0].equals("JMP") && !Utils.isInteger(args[2])) {
			Integer where = labels.get(args[2]);
			if (where == null)
				throw new Exception(""+(linen+1)+" ERROR: Undefined label '"+args[2]+"'");
			
			args[2] = ""+where;
		}
		
		final List<Byte> ret = new ArrayList<Byte>();
		
		final Op op = createOpcode(args[0], linen);
		
		// check number of arguments
		if ((op == Op.NOP && args.length != 1) ||
			(op == Op.INC && args.length != 2) || (op == Op.DEC && args.length != 2) || (op == Op.NOT && args.length != 2) ||		

			(op == Op.MOV && args.length != 3) || (op == Op.ADD && args.length != 3) || (op == Op.SUB && args.length != 3) ||		
			(op == Op.MUL && args.length != 3) || (op == Op.DIV && args.length != 3) || (op == Op.MOD && args.length != 3) ||		
			(op == Op.AND && args.length != 3) || (op == Op.OR && args.length != 3) || (op == Op.XOR && args.length != 3) ||		
			(op == Op.SHR && args.length != 3) || (op == Op.SHL && args.length != 3) || (op == Op.ROR && args.length != 3) ||		
			(op == Op.ROL && args.length != 3) || (op == Op.TEST && args.length != 3) || (op == Op.JMP && args.length != 3))
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
	
	private static Op createOpcode(String opcode, int linen) throws Exception {
		try {
			return Op.create(opcode);
		}
		catch (Exception e) {
			throw new Exception(""+(linen+1)+" "+e.getMessage());
		}
	}
	
	public static final int REF_INT_VALUE = 0xFFFFFFFF;
	public static final byte[] REF_VALUE = Utils.toByteArray(REF_INT_VALUE);

	public static List<Byte> compileValue(String line) {
		
		try {
			final List<Byte> ret = new ArrayList<Byte>();
			
			while (line.startsWith("#")) {
				line = line.substring(1);
				
				for (int k=0; k<REF_VALUE.length; k++)
					ret.add(REF_VALUE[k]);
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