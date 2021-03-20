package my;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import my.PacConfigLoader.OnFoundTextOrItemElement;
import my.util.Util;

public class PacSplitter {
	
	public static void main(String[] args) {
		split(Conf.pacDir, Conf.desktop+"beidousplit/");
	}
	
	public static void split(final String srcDir, final String dstDir){
		PacConfigLoader.loadExtractionConfig(new OnFoundTextOrItemElement() {
			
			@Override
			public void do_(String pac, Object... value) {
				try {
					if(pac.contains("/")) {
						String[] arr = pac.split("/");
						splitWithChildDir(arr[0], Integer.parseInt(arr[1]), Integer.parseInt((String)value[0]));
					}else {
						String _dstDir = dstDir+pac.replace(".PAC", "");
						splitNoChildDir(new File(srcDir+pac), _dstDir, Integer.parseInt((String)value[0]));
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
			//assert child pac's magic number=0x80
			private void splitWithChildDir(String pac, int childPacIndex, int grandsonPacIndex) throws IOException{
				RandomAccessFile f=new RandomAccessFile(srcDir+pac, "r");
				f.seek(8*childPacIndex);
				int offset=Util.hilo(f.readInt());	//align to 4bytes
				int len=Util.hilo(f.readInt());		//len after compressed. contains pacItem's head.
				f.seek(offset);
				byte[] childPac = new byte[len];
				f.read(childPac);
				f.close();
				
				File childPacRaw = new File(dstDir+pac.replace(".PAC", "")+"/"+childPacIndex+"/raw");
				childPacRaw.getParentFile().mkdirs();
				FileOutputStream fos=new FileOutputStream(childPacRaw);
				fos.write(childPac);
				fos.close();
				
				splitNoChildDir(childPacRaw, childPacRaw.getParent(), grandsonPacIndex);
				childPacRaw.delete();
			}
			
			
			private void splitNoChildDir(File srcPac, String dstDir, int pacItemIndex) throws IOException {
				RandomAccessFile f=new RandomAccessFile(srcPac, "r");
				f.seek(8*pacItemIndex);
				int offset=Util.hilo(f.readInt());	//align to 4bytes
				int len=Util.hilo(f.readInt());		//len after compressed. contains pacItem's head.
				PacItem item=PacItem.decompress(f, offset, len);
				
				File dst=new File(dstDir, ""+pacItemIndex);
				dst.getParentFile().mkdirs();
				
				FileOutputStream fos=new FileOutputStream(dst);
				fos.write(item.data);
				fos.close();
				
				fos=new FileOutputStream(dst+".head");
				fos.write(item.head);
				fos.close();
				
				f.close();
			}
		});
	}

}
