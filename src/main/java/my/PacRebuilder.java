package my;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import common.Util;
import my.PacConfigLoader.Callback;
import my.hack.Compressor;

public class PacRebuilder {
	
	public static void rebuild(final String pacDir, final String splitDir, final String dstDir){
		PacConfigLoader.loadExtractionConfig(new Callback() {
			
			@Override
			public void do_(String pac, Object... value) {
				try {
					int pacItem=Integer.parseInt((String)value[0]);
					String itemName=splitDir+pac.replace(".PAC", "")+File.separator+pacItem;
					
					RandomAccessFile patchHead=new RandomAccessFile(itemName+".head", "r");
					patchHead.seek(8);
					int posBits=patchHead.readByte();
					patchHead.close();
					
					FileInputStream patch=new FileInputStream(itemName);
					ByteArrayOutputStream compPatch=new ByteArrayOutputStream();
					new Compressor().compress(patch, compPatch, posBits);
					patch.close();
					
					Util.copyFile(pacDir+pac, dstDir+pac);
					RandomAccessFile pacF=new RandomAccessFile(dstDir+pac, "rw");
					pacF.seek(8*pacItem);
					int offset=Util.hilo(pacF.readInt());
					int len=Util.hilo(pacF.readInt());
					if(compPatch.size()>len) {
						String err=pac+"压缩后比原pac要大"+(compPatch.size()-len)+"字节,处理方法:根据translation/beidou.txt找出该pac中的所有文本,精简译文";
						System.err.println(err);
//						throw new UnsupportedOperationException(err);
					} else {
						pacF.seek(offset+16);
						pacF.write(compPatch.toByteArray());
					}
					pacF.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

}
