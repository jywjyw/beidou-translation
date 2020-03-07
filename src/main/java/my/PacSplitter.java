package my;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import common.Conf;
import common.Util;
import my.PacConfigLoader.Callback;

public class PacSplitter {
	
	public static void main(String[] args) {
		split(Conf.pacDir, Conf.desktop+"beidousplit/");
	}
	
	public static void split(final String srcDir, final String dstDir){
		PacConfigLoader.loadExtractionConfig(new Callback() {
			
			@Override
			public void do_(String pac, Object... value) {
				try {
					RandomAccessFile f=new RandomAccessFile(srcDir+pac, "r");
					int pacItem=Integer.parseInt((String)value[0]);
					f.seek(8*pacItem);
					int offset=Util.hilo(f.readInt());	//align to 4bytes
					int len=Util.hilo(f.readInt());		//len after compressed. contains pacItem's head.
					PacItem item=PacItem.load(f, offset, len);
					
					File dst=new File(dstDir+pac.replace(".PAC", "")+"/"+pacItem);
					dst.getParentFile().mkdirs();
					
					FileOutputStream fos=new FileOutputStream(dst);
					fos.write(item.data);
					fos.close();
					
					fos=new FileOutputStream(dst+".head");
					fos.write(item.head);
					fos.close();
					
					f.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

}
