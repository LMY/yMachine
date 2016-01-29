package y;

public class yMachine {
	public static void main(String args[])
	{
		try {
			System.out.println("Result is: "+executeFilename(2, 2, "asm/factN.asm"));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static long executeFilename(int regn, int outn, String filename) throws Exception {
		final String content = Utils.ReadWholeFile(filename);
		return execute(regn, outn, content);
	}
	
	public static long execute(int regn, int outn, String code) throws Exception {
		final byte[] program = Compiler.compile(code);
		return execute(regn, outn, program);
	}
	
	public static long execute(int regn, int outn, byte[] program) {
		final SimpleMachine machine = new SimpleMachine(regn + 1);
		machine.start(program);
		return machine.yield(outn);
	}
}
