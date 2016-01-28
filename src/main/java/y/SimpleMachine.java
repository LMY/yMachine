package y;

public class SimpleMachine {
	
	private boolean flag_e;
	private boolean flag_z;
	private boolean flag_g;
	private boolean flag_l;
	private boolean flag_o;
	
	private long[] registers;
	
	public SimpleMachine(int n) {
		registers = new long[n];
		
		flag_e = false;
		flag_z = false;
		flag_g = false;
		flag_l = false;
		flag_o = false;
	}
	
	public void start(byte[] program) {
		
		while (registers[0] < program.length)
			step(program);		
	}
	
	
	public void step(byte[] program) {
//		final int ip = (int) registers[0];
		
		final Op op = Op.create(program[(int) registers[0]++]);
		
System.out.println(""+op.toString());
		
		if (op == Op.NOP)
			return;
		
		final int reg1 = createReg(program);
System.out.println(""+reg1);
		
		if (op == Op.INC)
			registers[reg1]++;
		else if (op == Op.DEC)
			registers[reg1]--;	
		else if (op == Op.NOT)
			registers[reg1] = ~registers[reg1];
		else {
			final int value = createReg(program);
	System.out.println(""+value);	
	
			if (op == Op.MOV)
				registers[reg1] = value;
			else if (op == Op.ADD)
				registers[reg1] += value;
			else if (op == Op.SUB)
				registers[reg1] -= value;
			else if (op == Op.MUL)
				registers[reg1] *= value;
			else if (op == Op.DIV)
				registers[reg1] /= value;
			else if (op == Op.MOD)
				registers[reg1] %= value;
			
			else if (op == Op.AND)
				registers[reg1] &= value;
			else if (op == Op.OR)
				registers[reg1] |= value;
			else if (op == Op.XOR)
				registers[reg1] ^= value;
	
			else if (op == Op.SHR)
				registers[reg1] >>= value;
			else if (op == Op.SHL)
				registers[reg1] <<= value;
			else if (op == Op.ROR)				// TODO
				registers[reg1] >>= value;
			else if (op == Op.ROL)				// TODO
				registers[reg1] <<= value;
			
			else if (op == Op.TEST) {
				
				flag_z = registers[reg1]==0;
				flag_e = registers[reg1]==value;
				flag_g = registers[reg1]<value;
				flag_l = registers[reg1]>value;
				
				flag_o = false;
			}
			else if (op == Op.JMP) {
				final boolean req_z = (reg1&1) != 0;
				final boolean req_e = (reg1&2) != 0;
				final boolean req_g = (reg1&4) != 0;
				final boolean req_l = (reg1&8) != 0;
				
				final boolean req_nz = (reg1&16) != 0;
				final boolean req_ne = (reg1&32) != 0;
				final boolean req_ng = (reg1&64) != 0;
				final boolean req_nl = (reg1&128) != 0;
				
				if ((req_z && !flag_z) || (req_e && !flag_e) || (req_g && !flag_g) || (req_l && !flag_l) ||
					(req_nz && flag_z) || (req_ne && flag_e) || (req_ng && flag_g) || (req_nl && flag_l)
						)	// flag required, but not set
					;	// do not jump
				else
					registers[0] = value;
			}
		}
	}
	
	
	public int createReg(byte[] program) {
		
		int ret, ref_count = 0;
		
		while (true) {
			ret = createValue(program);
			
			if (ret == Op.REF_INT_VALUE)
				ref_count++;
			else
				break;
		}
		
		while (ref_count-- > 0)
			ret = (int) registers[ret];
		
		return ret;	
	}
	
	public int createValue(byte[] program) {
		return Op.fromByteArray(new byte[] {		// decode 4 byte -> int
				program[(int) registers[0]++],
				program[(int) registers[0]++],
				program[(int) registers[0]++],
				program[(int) registers[0]++] });
	}

	public void dump() {
		System.out.print("[");
		
		for (int i=0; i<registers.length; i++) {
			System.out.print(""+registers[i]);
			if (i != registers.length-1)
				System.out.print(",");
		}
			
		System.out.println("]");
	}
}
