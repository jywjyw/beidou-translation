package my.dump;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import my.util.Util;

public class BytesStream extends OutputStream {
	
	public static void main(String[] args) throws IOException {
//		byte[] buf=new byte[10];
//		Arrays.fill(buf, (byte)0xff);
//		System.out.println(Util.hexEncode(Arrays.copyOf(buf, 20)));
		BytesStream os=new BytesStream();
		for(int i=1;i<=13;i++){
			os.write(i);
			System.out.println(os.get(1));
		}
		System.out.println(Util.hexEncode(os.toByteArray()));
	}
	
    byte buf[]=new byte[5];
    int count;

	@Override
	public void write(int b) throws IOException {
		ensureCapacity(count + 1);
		buf[count] = (byte) b;
		count += 1;
	}
	
    public void write(byte b[], int off, int len) {
    	throw new UnsupportedOperationException();
//        if ((off < 0) || (off > b.length) || (len < 0) ||
//            ((off + len) - b.length > 0)) {
//            throw new IndexOutOfBoundsException();
//        }
//        ensureCapacity(count + len);
//        System.arraycopy(b, off, buf, count, len);
//        count += len;
    }
	
	private void ensureCapacity(int minCapacity) {
		 if (minCapacity > buf.length){
			int oldCapacity = buf.length;
	        int newCapacity = oldCapacity << 1;
	        buf = Arrays.copyOf(buf, newCapacity);
		 }
	}
	
	public byte toByteArray()[] {
		return Arrays.copyOf(buf, count);
	}
	
	public byte get(int backOffset){
		if(count<backOffset)
			System.out.println("dddddddddddd");
		return buf[count-backOffset];
	}
	
    public int size() {
        return count;
    }

}
