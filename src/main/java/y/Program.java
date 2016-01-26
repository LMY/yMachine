package y;

public class Program {
	private byte[] data;
	
	public Program(byte[] data) {
		this.data = data;
	}
	
	public byte getByte(long i) {
		return data[(int) i];
	}
	
	public long getLong(long i) {
		return data[(int) i]<<24 + data[(int) (i+1)]<<16 + data[(int) (i+2)]<<8 + data[(int) (i+3)];
	}
}
