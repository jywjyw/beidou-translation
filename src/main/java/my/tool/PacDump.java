package my.tool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import common.Conf;
import common.Img4bitUtil;
import common.Palette;
import common.Tuple2;
import common.Util;
import my.dump.Uncompressor;

public class PacDump {
	
	public static void main(String[] args) throws IOException {
//		parseOne("");
		loopParse();
	}
	
	public static void loopParse() throws IOException{
		for(File f:new File(Conf.pacDir).listFiles()){
			File dir=new File(Conf.desktop+"beidousplit/"+f.getName());
			if(!dir.exists()) dir.mkdir();
			parse(f, dir.getAbsolutePath());
		}
	}
	
	public static void parseOne(String pacName) throws IOException{
		File pac = new File(Conf.desktop+"beidou/"+pacName);
		File out = new File(Conf.desktop+"beidousplit/"+pac.getName());
		out.mkdir();
		parse(pac, out.getAbsolutePath());
	}
	
	public static void parse(File in, String outdir) throws IOException {
		RandomAccessFile pac=new RandomAccessFile(in, "r");
		List<Tuple2<Integer,Integer>> offset_size = new ArrayList<>();
		Integer offset0=null;
		while(true){
			int offset=Util.hilo(pac.readInt());
			int size = Util.hilo(pac.readInt());
			offset_size.add(new Tuple2<Integer,Integer>(offset, size));
			if(offset!=0&&offset0==null) offset0=offset;
			if(offset0!=null && pac.getFilePointer()>=offset0) break; 
		}
		
		int pacItemIndex=0;
		byte[] subpac;
		String name="";
		for(int i=0;i<offset_size.size();i++){
			Tuple2<Integer,Integer> e=offset_size.get(i);
			if(e.get1()!=0 && e.get2()!=0){	//TODO 有时候offset>0&&size=0
				subpac=new byte[e.get2()];
				pac.seek(e.get1());
				pac.read(subpac);
				
				try {
					if(subpac[0]==1)	{//compressed data
						name=pacItemIndex+"."+Integer.toHexString(subpac[0]);
						DataInputStream dis=new DataInputStream(new ByteArrayInputStream(subpac));
						dis.readInt();
						int uncompLen=Util.hilo(dis.readInt());
						int key=Util.hilo(dis.readInt())&0xffff;
						dis.readInt();//unk
						subpac=new Uncompressor().uncompress(dis, uncompLen, key);
						
						if(subpac[0]==0x10){
							dump(in.getName(), i, subpac);
						}
						
//				} else if(subpac[0]==0x80)	{//maybe means pac
					} else {
						name=pacItemIndex+".raw";
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} else {
				subpac=new byte[0];
				name=pacItemIndex+"";
			}
			
			pacItemIndex++;
			FileOutputStream os=new FileOutputStream(outdir+"/"+name);
			os.write(subpac);
			os.close();
		}
		
		pac.close();
	}
	
	private static void dump(String pac, int pacItemIndex, byte[] picpac) throws IOException{
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(picpac));
		dis.readInt(); // unk, used to control flow
		dis.readInt(); // unk, used to control flow

		int i = 0;
		while (true) {
			try {
				int picLen = Util.hilo(dis.readInt()); // means picBodySize+headLen = n+12
				int x = Util.hiloShort(dis.readShort());
				int y = Util.hiloShort(dis.readShort());
				int w = Util.hiloShort(dis.readShort());
				int h = Util.hiloShort(dis.readShort());
				// byte[] body=new byte[picLen-0xc];
				// dis.read(body);
				Palette pal=Palette.init16Grey();
//				Palette pal=new Palette(16, Conf.getRawFile("clut/304-498.16"));
				
				BufferedImage img = Img4bitUtil.readRomToBmp(dis, w, h, pal);
				ImageIO.write(img, "bmp", new File(String.format("%sbeidoudump/%s_%d_%d.bmp", Conf.desktop,pac,pacItemIndex,i++)));
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	

}
