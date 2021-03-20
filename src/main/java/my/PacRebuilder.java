package my;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import my.PacConfigLoader.OnFoundTextOrItemElement;
import my.hack.Compressor;
import my.util.Util;

public class PacRebuilder {
	
	public static void rebuild(final String pacDir, final String splitDir, final String dstDir){
		PacConfigLoader.loadExtractionConfig(new OnFoundTextOrItemElement() {
			
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
					
					if(pac.contains("/")) {
						String[] arr=pac.split("/");
						pac = arr[0];
						int childPacIndex = Integer.parseInt(arr[1]);
						int grandsonIndex = pacItem;
						Util.copyFile(pacDir+pac, dstDir+pac);
						RandomAccessFile pacF=new RandomAccessFile(dstDir+pac, "rw");
						pacF.seek(8*childPacIndex);	//寻找子pac的指针位置
						int childPacOffset=Util.hilo(pacF.readInt());
						int childPacLen=Util.hilo(pacF.readInt());
						
						pacF.seek(childPacOffset);
						pacF.skipBytes(8*grandsonIndex);
						int grandsonOffset=Util.hilo(pacF.readInt());
						int grandsonLen=Util.hilo(pacF.readInt());
						
						if(compPatch.size()>grandsonLen) {
							String err=pac+"压缩后比原pac要大"+(compPatch.size()-grandsonLen)+"字节,处理方法:根据translation/beidou.idx找出该pac中的所有文本,精简译文";
							System.err.println("pac rebuild failed: "+err);	
						}else {
							pacF.seek(childPacOffset+16);
							pacF.skipBytes(grandsonOffset);
							pacF.write(compPatch.toByteArray());
						}
						pacF.close();
					} else {
						Util.copyFile(pacDir+pac, dstDir+pac);
						RandomAccessFile pacF=new RandomAccessFile(dstDir+pac, "rw");
						pacF.seek(8*pacItem);
						int offset=Util.hilo(pacF.readInt());
						int len=Util.hilo(pacF.readInt());
						if(compPatch.size()>len) {
							String err=pac+"压缩后比原pac要大"+(compPatch.size()-len)+"字节,处理方法:根据translation/beidou.idx找出该pac中的所有文本,精简译文";
							System.err.println("pac rebuild failed: "+err);	
						} else {
							pacF.seek(offset+16);
							pacF.write(compPatch.toByteArray());
						}
						pacF.close();
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
	}

}
