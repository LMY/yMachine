package y;

import java.util.HashMap;
import java.util.Map;

public class SimpleMachine extends Thread {
	
	final static boolean DEBUG = Compiler.DEBUG;
	
	private boolean flag_e;
	private boolean flag_z;
	private boolean flag_g;
	private boolean flag_l;
//	private boolean flag_o;
	
	private long retv;
	private boolean running;
	private Exception exception;
	
	private long[] registers;
	private byte[] program;
	
	private Map<Integer, SimpleMachine> machines;
	
	
	public SimpleMachine() {
		this(new byte[0]);
	}
	
	public SimpleMachine(byte[] program) {
		running = true;					// in constructor and not in run()
										// because otherwise another machine calling FORK -> if (x.isRunning()) | before start()
										// would get false
		registers = new long[1];
		machines = new HashMap<Integer, SimpleMachine>();
		exception = null;
		
		load(program, true);
		
		flag_e = false;
		flag_z = false;
		flag_g = false;
		flag_l = false;
//		flag_o = false;
	}
	
	
	public synchronized void load(byte[] program, boolean restart) {
		this.program = program;
		if (restart && registers != null && registers.length > 0)
			registers[0] = 0;
	}
	
	public void run() {
		retv = 0;
		exception = null;
		
		try {
			while (registers[0] < program.length && running)
				step(program);
		}
		catch (Exception e) {
			exception = e;
			running = false;
		}
	}
	
	public synchronized void step(byte[] program) throws Exception {
		
		if (DEBUG)
			System.out.print("(0x"+(registers[0])+")");
	
		final int start_addr = (int) registers[0]++;
		final Op op = Op.create(program[start_addr]);
		try {		
			if (op == Op.NOP)
				return;
			
			final int reg1 = parseRegister(program);
	
			if (DEBUG)
				System.out.print("\t"+op.toString()+"\t"+reg1);
			
			//
			// OPS with 1 param
			//
			if (op == Op.INC)
				registers[reg1]++;
			else if (op == Op.DEC)
				registers[reg1]--;	
			else if (op == Op.NOT)
				registers[reg1] = ~registers[reg1];
			else if (op == Op.ALLOC) {
				if (registers.length < reg1+1) {	// realloc only if needed
					final long[] newregisters = new long[reg1+1];
					for (int i=0, imax=Math.min(registers.length, newregisters.length); i<imax; i++)
						newregisters[i] = registers[i];
					
					registers = newregisters;
				}
			}
			else if (op == Op.RETURN) {
				running = false;
				retv = reg1;
			}
			else if (op == Op.FORK) {
				final SimpleMachine newmachine = new SimpleMachine();
				machines.put(reg1, newmachine);
			}
			else if (op == Op.CLONE) {
				final SimpleMachine newmachine = new SimpleMachine();
				
				final long[] copyregs = new long[registers.length];
				for (int i=1; i<copyregs.length; i++)
					copyregs[i] = registers[i];
				newmachine.loadRegisters(copyregs);
				
				machines.put(reg1, newmachine);
			}
			else if (op == Op.FREE)
				machines.remove(reg1);
			else if (op == Op.START) {
				final SimpleMachine mac = machines.get(reg1);
				throwIfMachineDoesntExist(mac, reg1);
				
//				if (DEBUG) {
//					System.out.print("\nStaring new machine: ");
//					mac.dump();
//					System.out.println();
//				}
				
				mac.start(); 
			}
			else if (op == Op.JOIN) {
				final SimpleMachine mac = machines.get(reg1);
				throwIfMachineDoesntExist(mac, reg1);
				
				waitForMachine(mac);
				
				if (mac.isError())
					throw mac.getError();
			}
			else if (op == Op.KILL) {
				final SimpleMachine mac = machines.get(reg1);
				throwIfMachineDoesntExist(mac, reg1);
				mac.kill();
				machines.remove(reg1);			// FREE not required
			}
			else if (op == Op.PRINTCHAR) {
				System.out.print((char)reg1);
			}
			else if (op == Op.PRINTINT)
				System.out.print(reg1);
			else if (op == Op.PRINTSTRING) {
				
				int addr = reg1;
				
				while (true) {
					final long value = Utils.fromByteArray(new byte[] {		// decode 4 byte -> int
							program[addr++],
							program[addr++],
							program[addr++],
							program[addr++]});
					
					if (value == 0)
						break;
					else
						System.out.print((char)value);
				}
			}
			else if (op == Op.DATA)
				;	// NOP. Shouldn't exist a "DATA" opcode, anyway
			else if (op == Op.READCHAR)
				registers[reg1] = (char) System.in.read();
			else if (op == Op.READINT)
				registers[reg1] = Utils.readIntFromConsolle();
			
			//
			// OPS with 2 params
			//
			else {
				final int value = parseRegister(program);
				
				if (DEBUG)
					System.out.print(" "+value);
		
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
				else if (op == Op.ROR)
					registers[reg1] = Long.rotateRight(registers[reg1], value);
				else if (op == Op.ROL)
					registers[reg1] = Long.rotateLeft(registers[reg1], value);
				
				else if (op == Op.TEST) {
					flag_z = registers[reg1]==0;
					flag_e = registers[reg1]==value;
					flag_g = registers[reg1]<value;
					flag_l = registers[reg1]>value;
	//				flag_o = false;
				}
				else if (op == Op.JMP) {
					final boolean req_z = (reg1&JUMP_FLAG_Z) != 0;
					final boolean req_e = (reg1&JUMP_FLAG_E) != 0;
					final boolean req_g = (reg1&JUMP_FLAG_G) != 0;
					final boolean req_l = (reg1&JUMP_FLAG_L) != 0;
					
					final boolean req_nz = (reg1&JUMP_FLAG_NZ) != 0;
					final boolean req_ne = (reg1&JUMP_FLAG_NE) != 0;
					final boolean req_ng = (reg1&JUMP_FLAG_NG) != 0;
					final boolean req_nl = (reg1&JUMP_FLAG_NL) != 0;
					
					final boolean absolute = (reg1&JUMP_FLAG_ABSOLUTE) != 0;
					
					if (!((req_z && !flag_z) || (req_e && !flag_e) || (req_g && !flag_g) || (req_l && !flag_l) ||
						(req_nz && flag_z) || (req_ne && flag_e) || (req_ng && flag_g) || (req_nl && flag_l)))	{// required flags ok
						
						if (absolute)
							registers[0] = value;
						else
							registers[0] += value;
					}
				}
				//
				// OPS with 3 params
				//
				else {
					final int value2 = parseRegister(program);
					
					if (DEBUG)
						System.out.print(" "+value2);
					
					if (op == Op.IN) {
						final SimpleMachine mac = machines.get(reg1);
						throwIfMachineDoesntExist(mac, reg1);
						
						loadRegister(value, mac.readRegister(value2));
					}
					else if (op == Op.OUT) {
						final SimpleMachine mac = machines.get(reg1);
						throwIfMachineDoesntExist(mac, reg1);
						
						mac.loadRegister(value2, value);
					}
					else if (op == Op.LOADCODE) {
						final SimpleMachine mac = machines.get(reg1);
						throwIfMachineDoesntExist(mac, reg1);
						
						final int base = (int) (registers[0]+value-4);
						
						final byte[] subprog = new byte[value2];
						for (int i=0; i<subprog.length; i++)
							subprog[i] = program[base + i];
						mac.load(subprog, false);
					}
				}
			}
			
			if (DEBUG)
				System.out.println();
		}
		catch (Exception e) {
			throw new Exception("@"+start_addr+" ("+op.toString()+") "+e.toString());
		}
	}
	
	private void waitForMachine(SimpleMachine mac) {
		while (mac.isRunning()) {
			try { Thread.sleep(100); }
			catch (InterruptedException e) {}
		}
	}

	private void throwIfMachineDoesntExist(SimpleMachine mac, long reg1) throws Exception {
		if (mac == null)
			throw new Exception("Machine "+reg1+" doesn't exist");
	}
	
	private int parseRegister(byte[] program) {
		int ref_count = 0;
		
		while (true) {
			int ret = parseValue(program);
			
			if (ret == Compiler.REF_INT_VALUE)
				ref_count++;
			else {
				while (ref_count-- > 0)
					ret = (int) registers[ret];
				return ret;
			}
		}
	}
	
	private int parseValue(byte[] program) {
		return Utils.fromByteArray(new byte[] {		// decode 4 byte -> int
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
	
	public synchronized long readRegister(int n) {
		return registers[n];
	}

	public synchronized void loadRegister(int n, long value) {
		if (registers == null)
			registers = new long[n+1];
		if (registers.length < n+1) {
			final long[] newregisters = new long[n+1];
			for (int i=0; i<registers.length; i++)
				newregisters[i] = registers[i];

			registers = newregisters;
		}
		
		registers[n] = value;
	}
	
	public synchronized void loadRegisters(long[] registers) {
		this.registers = registers;
	}
	
	public synchronized boolean isRunning() {
		return running; 
	}
	
	public boolean isError() {
		return exception != null;
	}
	
	public Exception getError() {
		return exception;
	}
	
	public long getReturnValue() {
		return retv;
	}
	
	public synchronized boolean kill() {
		if (!running)
			return false;
		
		running = false;
		return true;
	}
	
	public final static int JUMP_FLAG_Z = 1;
	public final static int JUMP_FLAG_E = 2;
	public final static int JUMP_FLAG_G = 4;
	public final static int JUMP_FLAG_L = 8;

	public final static int JUMP_FLAG_NZ = 16;
	public final static int JUMP_FLAG_NE = 32;
	public final static int JUMP_FLAG_NG = 64;
	public final static int JUMP_FLAG_NL = 128;
	
	public final static int JUMP_FLAG_ABSOLUTE = 256;
}
