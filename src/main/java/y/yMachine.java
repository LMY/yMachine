package y;

public class yMachine {
	
	public final static String sumFivePlusFour = "MOV 1 5\nMOV 2 4\nADD 1 #2";
	public final static String sumN = "MOV 1 10\nADD 2 #1\nDEC 1\nTEST 1 #1\nJMP 16 9";
	public final static String factN = "MOV 1 10\nMOV 2 1\nMUL 2 #1\nDEC 1\nTEST 1 #1\nJMP 16 18";
	
	public static void main(String args[])
	{
		final byte[] code = Op.compile(factN);
		
		System.out.println("REF INT IS: "+Op.REF_INT_VALUE);
		System.out.println("REF[0]: "+Op.REF_VALUE[0]);
		System.out.println("REF[1]: "+Op.REF_VALUE[1]);
		System.out.println("REF[2]: "+Op.REF_VALUE[2]);
		System.out.println("REF[3]: "+Op.REF_VALUE[3]);
		
		final SimpleMachine machine = new SimpleMachine(10);
		machine.start(code);
		machine.dump();
	}
}
