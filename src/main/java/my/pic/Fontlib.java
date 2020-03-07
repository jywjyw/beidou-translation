package my.pic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import common.Conf;
import common.Img4bitUtil;
import common.Palette;
import common.VramImg;
import my.Picpack;

public class Fontlib {
	
	//START.PAC -> 8 -> 1
	public void export(String splitDir, String targetDir) throws IOException{
		VramImg vram=Picpack.extract(new File(splitDir+"START/8"), 1);
		for(int clutY:new int[]{496,497,498,499}){
			Palette pal=new Palette(16, Conf.getRawFile("clut/352-"+clutY+".16"));
			BufferedImage img=Img4bitUtil.readRomToBmp(new ByteArrayInputStream(vram.data), vram.w, vram.h, pal);
			ImageIO.write(img, "bmp", new File(targetDir+"font-"+clutY+".bmp"));
		}
	}
	
	public void import_(String splitDir, VramImg vram) throws IOException{
		Picpack.replace(new File(splitDir+"START/8"), 1, vram.data);
	}
	

}
