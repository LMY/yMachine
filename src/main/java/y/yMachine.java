package y;

public class yMachine {
	
	public final static String sumFivePlusFour = "MOV 1 5\nMOV 2 4\nADD 1 #2";
	public final static String sumN = "MOV 1 10\nADD 2 #1\nDEC 1\nTEST 1 #1\nJMP 16 9";
	public final static String factN = "MOV 1 10\nMOV 2 1\nMUL 2 #1\nDEC 1\nTEST 1 #1\nJMP 16 18";
	
	public static void main(String args[])
	{
//		final byte[] code = Op.compile(factN);
//		
//		final SimpleMachine machine = new SimpleMachine(10);
//		machine.start(code);
//		machine.dump();
		
		System.out.println("Result is: "+execute(2, 2, factN));
	}
	
	public static long execute(int regn, int outn, String code) {
		final byte[] program = Op.compile(code);
		return execute(regn, outn, program);
	}
	
	public static long execute(int regn, int outn, byte[] program) {
		final SimpleMachine machine = new SimpleMachine(regn + 1);
		machine.start(program);
		return machine.yield(outn);
	}
}
