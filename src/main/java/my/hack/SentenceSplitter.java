package my.hack;

import java.io.IOException;
import java.io.StringReader;

import my.util.Util;


public class SentenceSplitter {
	
	public static void main(String[] args) throws IOException {
		SentenceSplitter.splitToWords("やぁ、キ[music]ミか{82}。", new Callback() {
			@Override
			public void onReadWord(boolean isCtrl, String word) {
				System.out.println(word);
			}
		});
	}
	
	public interface Callback{
		void onReadWord(boolean isCtrl, String word);
	}
	
	public static void splitToWords(String sentence, Callback cb) {
		StringReader r=new StringReader(sentence);
		StringBuilder unit = new StringBuilder();
		char[] buf=new char[1];
		int mode = 0; //0=normal, 控制符=1, 特殊字(图标)=2;
		try {
			while(r.read(buf)!=-1) {
				char c=buf[0];
				if(mode==1) {
					unit.append(c);
					if(c=='}') {
						mode=0;
						cb.onReadWord(true, unit.toString());
					}
				} else if(mode==2){
					unit.append(c);
					if(c==']') {
						mode=0;
						cb.onReadWord(false, unit.toString());
					}
				} else {
					if(c=='{') {
						mode=1;
						unit=new StringBuilder();
						unit.append(c);
					} else if(c=='['){
						mode=2;
						unit=new StringBuilder();
						unit.append(c);
					} else {
						cb.onReadWord(false, c+"");
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally{
			Util.close(r);
		}
	}

}
