package my.tool;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import my.Conf;
import my.dump.Charset;
import my.dump.ScriptReader;
import my.dump.Sentence;
import my.dump.Decompressor;

public class RawTextChecker {
	
	public static void mainx(String[] args) throws Exception {
		Charset cs=new Charset();
		String file = Conf.desktop+"beidousplit\\STAGE.PAC\\0.raw";
		RandomAccessFile f = new RandomAccessFile(file, "r");
		f.seek(0x3d55);
		new ScriptReader().readUtilEOF(f, cs, new ScriptReader.Callback(){
			@Override
			public boolean onReadedSentence(String s, int startAddr, int len, long filePos) {
				System.out.println(s);
				return false;
			}
		});
		
	}
	
	public static void main(String[] args) throws Exception {
		String file = Conf.desktop+"beidousplit\\STAGE.PAC\\0.raw";
		FileInputStream is=new FileInputStream(file);
		
		FileOutputStream fos=new FileOutputStream(Conf.desktop+"ddddd");
		fos.write(new Decompressor().uncompress(is, 5688, 12));
		fos.close();
		
		is.close();
		new Decompressor().uncompress(is, 500, 12);
	}

}
