package huffman;

public class CodeSymbol {
	long code;
	int length;

	public CodeSymbol(long code, int length) {
		this.code = code;
		this.length = length;
	}

	@Override
	public String toString() {
		String s1 = String.format("%8s",
				Long.toBinaryString(code)).replace(' ', '0');
		return s1 + ", length:" + length + ", ";
	}

}