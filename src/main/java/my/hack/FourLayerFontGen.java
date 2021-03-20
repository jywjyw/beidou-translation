package my.hack;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;

import my.Conf;
import my.util.Img4bitUtil;
import my.util.Palette;
import my.util.VramImg;
import my.util.VramImg.VramImg4bitWriter;

public class FourLayerFontGen {
	
	private static int[] 
			BLACK = new int[]{0,0,0},
			WHITE = new int[]{255,255,255};
	
	public static VramImg gen(List<List<BufferedImage>> tiles, int w, int h){
		int column=21;
		VramImg vram = new VramImg4bitWriter(w, h).build();
		for(int i=0;i<4;i++){
			BufferedImage layer=Img4bitUtil.jointTiles(tiles.get(i), column, w-column*12, 0, WHITE);	//TODO 4->0
			vram = addLayer(layer, vram, i+1);
		}
		return vram;
	}
	
	private static VramImg addLayer(BufferedImage img, VramImg lastLayer, int layerNum){
		Iterator<Byte> it=lastLayer.get4bitIterator();
		VramImg4bitWriter ret = new VramImg4bitWriter(img.getWidth(), img.getHeight());
		int[] pixel=new int[4];
		for(int y=0;y<img.getHeight();y++){
			for(int x=0;x<img.getWidth();x++){	
				img.getRaster().getPixel(x, y, pixel);
				byte last=it.next();
				if(pixel[0]==BLACK[0]){ //字体色
					ret.addPixelIndex(last | 1<<(4-layerNum));	//layerNum start from 1
				}else if(pixel[0]==WHITE[0]){ //背景色
					ret.addPixelIndex(last);
				}else{
					throw new RuntimeException("unreconized color");
				}
			}
		}
		return ret.build();
	}
	
	public static List<Palette> build4Palette(short fontColor){
		List<Palette> pals=new ArrayList<>();
		short bgColor=0;
		ByteBuffer pal=ByteBuffer.allocate(32);
		for(int i=0;i<8;i++){
			pal.putShort(bgColor);
		}
		for(int i=8;i<16;i++){
			pal.putShort(fontColor);
		}
		pals.add(new Palette(16, pal.array()));
		pal.clear();
		
		for(int i=0;i<2;i++){
			for(int j=0;j<4;j++) pal.putShort(bgColor);
			for(int k=0;k<4;k++) pal.putShort(fontColor);
		}
		pals.add(new Palette(16, pal.array()));
		pal.clear();
		
		for(int i=0;i<4;i++){
			pal.putShort((short) 0);
			pal.putShort((short) 0);
			pal.putShort(fontColor);
			pal.putShort(fontColor);
		}
		pals.add(new Palette(16, pal.array()));
		pal.clear();
		
		for(int i=0;i<8;i++){
			pal.putShort(bgColor);
			pal.putShort(fontColor);
		}
		pals.add(new Palette(16, pal.array()));
		pal.clear();
		
		return pals;
	}

}
