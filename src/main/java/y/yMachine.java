package y;

public class yMachine {
	public static void main(String args[])
	{
		try {
			System.out.println(""+executeFilename("asm/factN.asm"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
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
	
	public static long execute(byte[] program) {
		final SimpleMachine machine = new SimpleMachine(program);
		return machine.start();
	}
}
