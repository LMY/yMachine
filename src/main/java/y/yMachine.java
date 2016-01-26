package y;

import java.util.ArrayList;
import java.util.List;

public class yMachine {
	private long[] registers;
	private Program program;
	
	
	public yMachine(int n) {
		load(null);
	}
	
	public void restart(int n) {
		registers = new long[n];
		registers[0] = 0;
	}

	public void restart() {
		registers = new long[registers.length];
		registers[0] = 0;
	}

	
	public void load(Program program) {
		this.program = program;
		restart();
	}
	
	public void load(Program program, int n) {
		this.program = program;
		restart(n);
	}
	
	public void start() {
		
	}
	
	public void step() {
		final long ip = registers[0];
		
		// fetch
		final long opcode = program.getLong(ip);
		
		// decode
		
		// execute
		
		registers[0] = ip+4;
	}
}
