package my.util;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;

public class Bytes{
	public byte[] bytes;
	public Bytes(byte[] bytes) {
		this.bytes = bytes;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(bytes);
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bytes other = (Bytes) obj;
		if (!Arrays.equals(bytes, other.bytes))
			return false;
		return true;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(Util.hexEncode("a".getBytes("ascii")));
		System.out.println(Util.hexEncode("𠀀".getBytes("utf-8")));
		System.out.println(Util.hexEncode("一一一一一一一一一一一一".getBytes("utf-16")));
		System.out.println(Util.hexEncode("😀".getBytes("utf-16")));
		System.out.println(Util.hexEncode("𠀀".getBytes("utf-32")));
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream("d:/a.txt"), "utf-32"));
		pw.print("一一");
		pw.close();
	}
}