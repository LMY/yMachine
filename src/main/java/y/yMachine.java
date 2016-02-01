package y;

public class yMachine {
	public static void main(String args[])
	{
		try {
			System.out.println(""+executeFilename("asm/callSubr.asm"));
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
		return machine.start();
	}
}
