package y;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compiler {
	
	final static boolean DEBUG = true;
	
	private Map<String,Integer> labels_declared;
	private List<JumpPoint> labels_used;
	
	private List<Object[]> labels_lengths_used;
	private Map<String,Integer> labels_lengths_declared;
	
	public Compiler() {
		labels_declared = new HashMap<String,Integer>();
		labels_used = new ArrayList<JumpPoint>();
		labels_lengths_used = new ArrayList<Object[]>();
		labels_lengths_declared = new HashMap<String,Integer>();
	}
	
	public static byte[] compile(String text) throws Exception {
		final List<Byte> compiled = compileList(text);
		
		final byte[] ret = new byte[compiled.size()];
		for (int i=0; i<ret.length; i++)
			ret[i] = compiled.get(i);
		
		return ret;
	}
	
	private static List<Byte> compileList(String text) throws Exception {
		final Compiler compiler = new Compiler();
		return compiler.compileLines(text);
	}
	
	
	
	private List<Byte> compileLines(String text) throws Exception {
		final String[] lines = text.split("\n");
		
		final List<Byte> compiled = new ArrayList<Byte>();
		
		for (int linen=0; linen<lines.length; linen++) {
			final String line = removeComments(lines[linen]);
			
			if (line.isEmpty())
				continue;
			
			final int addr = compiled.size();
			final List<Byte> onelinecompiled = compileLine(line, linen, addr);
			if (onelinecompiled != null) {
				compiled.addAll(onelinecompiled);
				
				if (DEBUG)
					dump_compiled_line(addr, line, onelinecompiled);
			}
		}
		
		if (DEBUG) 
			dump_compiled(compiled);

		adjourn_calls(compiled);
		
		if (DEBUG) {
			System.out.println("After adjourn_calls()");
			dump_compiled(compiled);
		}
		
		return compiled;
	}
	
	private String removeComments(String text) {
		final int pos = Math.max(text.indexOf("//"), text.indexOf(";"));
		return (pos < 0 ? text : text.substring(0, pos-1)).trim();
	}

	private List<Byte> compileLine(String line, int linen, int addr) throws Exception {
		
		// save labels
		if (line.contains(":")) {
			final String[] p = line.split(":");
			if (p.length > 2)
				throw new Exception(""+(linen+1)+" ERROR: Multiple labels (or usage of ':'s)");

			// commented = labels can be redefined
			if (labels_declared.get(p[0]) != null)
				throw new Exception(""+(linen+1)+" ERROR: Label '"+p[0]+"' already defined at "+labels_declared.get(p[0]));
			
			labels_declared.put(p[0].trim(), addr);
			
			if (p.length == 1)
				return new ArrayList<Byte>();	// nothing to compile
			line = p[1].trim();
		}
		
		String[] args = Utils.splitAndTrim(line, "[\\s,]");

		// INCLUDE
		if (args[0].equals(OP_INCLUDE)) {
			if (args.length < 2)
				throw new Exception(""+(linen+1)+" ERROR: "+OP_INCLUDE+" with no parm");
			
			// create a new compiler unit (label scope is thus per file)
			// using line and not args[2] to preserve spaces in filename
			return Compiler.compileList(Utils.ReadWholeFile(line.substring(OP_INCLUDE.length()).trim()));
		}
		
		// END PROCEDURE
		if (args[0].equals(OP_ENDP)) {
			if (args.length < 2)
				throw new Exception(""+(linen+1)+" ERROR: "+OP_ENDP+" with no parm");
			if (args.length > 2)
				throw new Exception(""+(linen+1)+" ERROR: "+OP_ENDP+" with too many parms");
			
			final Integer startaddr = labels_declared.get(args[1]);
			if (startaddr == null)
				throw new Exception(""+(linen+1)+" ERROR: "+OP_ENDP+" found, but label "+args[1]+" does not exist");
			
			labels_lengths_declared.put(args[1], addr-startaddr);
			
			return new ArrayList<Byte>();	// nothing to compile
		}
		
		// decode conditional JMPs
		if (args[0].startsWith("J") && !args[0].equalsIgnoreCase("JOIN") && args.length == 2) {
			
			if (args[0].equals("JMP")) {
				args = new String[] { "JMP", "0", args[1] }; 
			}
			else {
				final String orig = args[0];
				args = new String[] { "JMP", "", args[1] }; 
				
				int reg = 0;
				boolean negate = false;
				
				for (int i=1; i<orig.length(); i++)
					switch (orig.charAt(i)) {
						case 'N': negate = true; break;
						case 'Z': reg |= (negate?16:1);  negate = false; break;
						case 'E': reg |= (negate?32:2);  negate = false; break;
						case 'G': reg |= (negate?64:4);  negate = false; break;
						case 'L': reg |= (negate?128:8);  negate = false; break;
						default : throw new Exception(""+(linen+1)+" ERROR: Invalid jump number ("+orig.charAt(i)+")");
					}

				if (args[2].startsWith("&"))	// absolute jump
					reg += 256;
				
				args[1] = ""+reg;
			}
		}
		
		final List<Byte> ret = new ArrayList<Byte>();
		
		final Op op = createOpcode(args[0], linen);
		
		// check number of arguments
		if ((op == Op.NOP && args.length != 1) ||
			(op == Op.INC && args.length != 2) || (op == Op.DEC && args.length != 2) || (op == Op.NOT && args.length != 2) ||
			(op == Op.ALLOC && args.length != 2) || (op == Op.RETURN && args.length != 2) ||
			(op == Op.DATA && args.length != 2) || (op == Op.PRINTCHAR && args.length != 2) || (op == Op.PRINTINT && args.length != 2) || (op == Op.PRINTSTRING && args.length != 2) || 
			
			(op == Op.CLONE && args.length != 2) || (op == Op.FORK && args.length != 2) || (op == Op.START && args.length != 2) || (op == Op.JOIN && args.length != 2) ||
			(op == Op.KILL && args.length != 2) || (op == Op.FREE && args.length != 2) ||
			(op == Op.LOADCODE && args.length != 3 && args.length != 4) ||
			(op == Op.IN && args.length != 4) || (op == Op.OUT && args.length != 4) ||

			(op == Op.MOV && args.length != 3) || (op == Op.ADD && args.length != 3) || (op == Op.SUB && args.length != 3) ||
			(op == Op.MUL && args.length != 3) || (op == Op.DIV && args.length != 3) || (op == Op.MOD && args.length != 3) ||
			(op == Op.AND && args.length != 3) || (op == Op.OR && args.length != 3) || (op == Op.XOR && args.length != 3) ||
			(op == Op.SHR && args.length != 3) || (op == Op.SHL && args.length != 3) || (op == Op.ROR && args.length != 3) ||
			(op == Op.ROL && args.length != 3) || (op == Op.TEST && args.length != 3) || (op == Op.JMP && args.length != 3))
			throw new Exception(""+(linen+1)+" ERROR: Invalid arg number ("+args.length+")");
		
		if (op != Op.DATA)
			ret.add(op.getCode());
		
		for (int i=1; i<args.length; i++) {
			if (isCompilableValue(args[i]))
				ret.addAll(compileValue(args[i]));
			else {
				if (args[2].charAt(0) == '&')
					labels_used.add(new JumpPoint(args[2].substring(1), addr+ret.size(), addr+ret.size()+4, true));
				else
					labels_used.add(new JumpPoint(args[2], addr+ret.size(), addr+ret.size()+4, false));
				ret.addAll(compileValue(""+DUMMY_INT_VALUE));				
			}
		}
		
		// LOADCODE LENGTH
		if (op == Op.LOADCODE && args.length == 3) {
			labels_lengths_used.add(new Object[]{args[2], (long)(addr+ret.size())});
			ret.addAll(compileValue(""+DUMMY_INT_VALUE));
		}
		return ret;
	}
	
	private void adjourn_calls(List<Byte> ret) throws Exception {
		for (JumpPoint p : labels_used) {
			final Integer where = labels_declared.get(p.first);
			if (where == null)
				throw new Exception("ERROR: Undefined label '"+p.first+"'");
			
			final List<Byte> addr = compileValue(""+(p.absolute ? where : where-p.third));
			
			for (int i=0; i<4; i++)
				ret.set(p.second+i, addr.get(i));
		}
		
		for (Object[] pair: labels_lengths_used) {
			final String label = (String)pair[0];
			final Integer where = labels_lengths_declared.get(label);
			if (where == null)
				throw new Exception("ERROR: Undefined length '"+label+"'");
			
			final List<Byte> addr = compileValue(""+where);
			
			final long base = (long)pair[1];
			
			for (int i=0; i<4; i++)
				ret.set((int) (base+i), addr.get(i));
		}
	}
	
	private static Op createOpcode(String opcode, int linen) throws Exception {
		try {
			return Op.create(opcode);
		}
		catch (Exception e) {
			throw new Exception(""+(linen+1)+" "+e.getMessage());
		}
	}
	
	public static final int DUMMY_INT_VALUE = 0x63636363;
	public static final int REF_INT_VALUE = 0xFFFFFFFF;
	public static final byte[] REF_VALUE = Utils.toByteArray(REF_INT_VALUE);

	
	public static boolean isCompilableValue(String text) {
		return text.matches("#*\\d*");
	}
	
	
	public static List<Byte> compileValue(String line) {
		
		try {
			final List<Byte> ret = new ArrayList<Byte>();
			
			while (line.startsWith("#")) {
				line = line.substring(1);
				appendArray(ret, REF_VALUE);
			}
			
			appendArray(ret, intToByteArray(Integer.parseInt(line)));

			return ret;
		}
		catch (Exception e) { return new ArrayList<Byte>(); }
	}
	
	public static void appendArray(List<Byte> theList, byte[] theArray) {
		for (int k=0; k<theArray.length; k++)
			theList.add(theArray[k]);
	}
	
	public static final byte[] intToByteArray(int value) {
	    return new byte[] { (byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value };
	}
	
	private void dump_compiled(List<Byte> ret) {
		System.out.print("Compiled: [");
		
		for (int i=0; i<ret.size(); i++) {
			System.out.print(""+((int) ret.get(i)));
			System.out.print(i == ret.size()-1 ? "]\n" : ", ");
		}
	}
	
	private void dump_compiled_line(int addr, String line, List<Byte> compiled) {
		System.out.print(""+addr+"\t\t"+line+"\t[");
		
		for (int i=0; i<compiled.size(); i++) {
			System.out.print(""+((int) compiled.get(i)));
			System.out.print(i == compiled.size()-1 ? "]\n" : ", ");
		}
	}
	
	private final static String OP_INCLUDE = "INCLUDE";
	private final static String OP_ENDP = "END";
}
