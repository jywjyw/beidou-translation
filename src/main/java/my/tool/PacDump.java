package my.tool;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import my.Conf;
import my.dump.Decompressor;
import my.util.Img4bitUtil;
import my.util.Palette;
import my.util.Tuple2;
import my.util.Util;

public class PacDump {
	
	public static void main(String[] args) throws IOException {
//		parseOne(new File(Conf.pacDir+"TITLE.PAC"));	//title,title2,stage
//		parseOne(new File(Conf.desktop+"beidousplit\\TITLE.PAC\\0.raw80"));	//title,title2,stage
		loopParse();
	}
	
	public static void loopParse() throws IOException{
		for(File f:new File(Conf.pacDir).listFiles()){
			if(f.getName().equals(Conf.EXE)) continue;
			File dir=new File(Conf.desktop+"beidousplit/"+f.getName());
			if(!dir.exists()) dir.mkdir();
			parse(f, dir.getAbsolutePath());
		}
	}
	
	public static void parseOne(File pac) throws IOException{
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
		byte[] pacItem;	//种类:压缩文件,未压缩文件,文件夹
		for(int i=0;i<offset_size.size();i++){
			Tuple2<Integer,Integer> e=offset_size.get(i);
			if(e.get1()!=0 && e.get2()!=0){	//TODO 有时候offset>0&&size=0
				pacItem=new byte[e.get2()];
				pac.seek(e.get1());
				pac.read(pacItem);
				
				try {
					if(getMagicNumber(pacItem)==1)	{//1=compressed data
						DataInputStream dis=new DataInputStream(new ByteArrayInputStream(pacItem));
						dis.readInt();
						int uncompLen=Util.hilo(dis.readInt());
						int key=Util.hilo(dis.readInt())&0xffff;
						dis.readInt();//unk
						pacItem=new Decompressor().uncompress(dis, uncompLen, key);
						save(outdir+"/"+pacItemIndex+".1", pacItem);
						if(pacItem[0]==0x10){
//							dump(in.getName(), i, pacItem);
						}
						
					} else if(getMagicNumber(pacItem)==0x80)	{//children directory
						System.out.println("含有80: pacItemIndex="+pacItemIndex+",pac="+in.getName());
						File tempRawFile = new File(outdir + "/"+pacItemIndex+".raw80");
						save(tempRawFile.getPath(), pacItem);
						parse(tempRawFile, outdir+"/"+pacItemIndex);
						tempRawFile.delete();
						
					} else {
						save(outdir+"/"+pacItemIndex+".raw", pacItem);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			} else {
				//just blank file
				save(outdir+"/"+pacItemIndex+".null", new byte[] {0});
			}
			pacItemIndex++;
		}
		pac.close();
	}
	
	private static int getMagicNumber(byte[] pacItem) {
		ByteBuffer buf = ByteBuffer.wrap(pacItem);
		return Util.hilo(buf.getInt());
	}
	
	
	private static void save(String filePath, byte[] content) {
		try {
			File f = new File(filePath);
			if(!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			FileOutputStream os=new FileOutputStream(f);
			os.write(content);
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
