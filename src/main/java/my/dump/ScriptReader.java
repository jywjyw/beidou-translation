package my.dump;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 读取ROM中的文本区
 */
public class ScriptReader {
	
	public interface Callback {
		public boolean onReadedSentence(String s, int startAddr, int len, long filePos);
	}
	
	private StringBuilder sentence = new StringBuilder();
	private int startAddr=0,len=0;
	
	public void readUtilEOF(RandomAccessFile in, Charset cs, Callback cb) throws IOException{
		startAddr=(int) in.getFilePointer();
		int readed;
		while(true) {
			readed=in.read();
			if(readed==0x82) {
				len++;
				sentence.append(String.format("{%02X}", readed));
			} else {
				int b=in.read();
				readed = (readed<<8)|b;
				len+=2;
				String char_ = cs.getChar(readed);
				if(char_==null) char_=String.format("{%04X}", readed);
				sentence.append(char_);
				if(readed==0xff00){
					try {
						short next = in.readShort();
						if(next==0) {
							len+=2;
						}else{
							in.seek(in.getFilePointer()-2);
						}
					} catch (EOFException e) {//ignore
					}
					if(!cb.onReadedSentence(sentence.toString(), startAddr, len, in.getFilePointer())) 
						break;
					sentence=new StringBuilder();
					startAddr=(int) in.getFilePointer();
					len=0;
				}
			}
		}
	}
	
	
}
