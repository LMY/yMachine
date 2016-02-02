package y;

public class yMachine {
	public static void main(String args[])
	{
		try {
			final long retv = executeFilename("asm/callSubr1.asm");
			System.out.println(""+retv);
		}
		catch (Exception e) {
			System.out.println("ERROR: "+e.getMessage()+"\n");
			System.out.println(e.toString());
		}
	}
	
	public static long executeFilename(String filename) throws Exception {
		final String content = Utils.ReadWholeFile(filename);
		return execute(content);
	}
	
	public static long execute(String code) throws Exception {
		final byte[] program = Compiler.compile(code);
		return execute(program);
	}
	
	public static long execute(byte[] program) throws Exception {
		
		final SimpleMachine machine = new SimpleMachine(program);

		machine.run();	// main machine is executed in main thread 
		
		if (machine.isError())
			throw machine.getError();
		else
			return machine.getReturnValue();
	}
}
