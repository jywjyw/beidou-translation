package my;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

//RandomAccessFile > InputStream
public class InputStreamAdapter extends InputStream{
	
	RandomAccessFile f;
	
	public InputStreamAdapter(RandomAccessFile f) {
		this.f = f;
	}

	@Override
	public int read() throws IOException {
		return f.read();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return f.read(b);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return f.read(b,off,len);
	}

	@Override
	public void close() throws IOException {
		f.close();
	}
	
	@Override
	public long skip(long n) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int available() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() throws IOException {
		throw new UnsupportedOperationException();
	}
	
	

}
