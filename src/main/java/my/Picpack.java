package my;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import my.util.Util;
import my.util.VramImg;

public class Picpack {
	
	public static VramImg extract(File f, int picIndex) throws IOException{
		RandomAccessFile file=new RandomAccessFile(f, "r");
		file.skipBytes(8);
		int i = 0;
		while (true) {
			try {
				int picLen = Util.hilo(file.readInt()); // means picBodySize+headLen = n+12
				int x = Util.hiloShort(file.readShort());
				int y = Util.hiloShort(file.readShort());
				int w = Util.hiloShort(file.readShort());
				int h = Util.hiloShort(file.readShort());
				if(i==picIndex) {
					byte[] data=new byte[picLen-12];
					file.read(data);
					file.close();
					return new VramImg(w,h,data);
				} else {
					file.skipBytes(picLen-12);
				}
				i++;
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		file.close();
		throw new UnsupportedOperationException("unk pic index:"+picIndex);
	}
	
	public static void replace(File f, int picIndex, byte[] newdata) throws IOException{
		RandomAccessFile file=new RandomAccessFile(f, "rw");
		file.skipBytes(8);
		int i = 0;
		while (true) {
			try {
				int picLen = Util.hilo(file.readInt()); // means picBodySize+headLen = n+12
				int x = Util.hiloShort(file.readShort());
				int y = Util.hiloShort(file.readShort());
				int w = Util.hiloShort(file.readShort());
				int h = Util.hiloShort(file.readShort());
				if(i==picIndex) {
					if(picLen-12!=newdata.length)
						throw new UnsupportedOperationException("替换图像的大小不对");
					file.write(newdata);
					file.close();
					return;
				} else {
					file.skipBytes(picLen-12);
				}
				i++;
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		file.close();
		throw new UnsupportedOperationException("unk pic index:"+picIndex);
	}
	
}
