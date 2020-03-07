package my.hack;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import common.Conf;
import common.Img4bitUtil;
import common.IsoPatcher;
import common.Palette;
import common.Util;
import common.VramImg;
import my.PacRebuilder;
import my.PacSplitter;
import my.pic.Fontlib;

public class Hack {
	
	public static void main(String[] args) throws IOException {
		String exe=Conf.outDir+Conf.EXE;
		Util.copyFile(Conf.pacDir+Conf.EXE, exe);
		
		String splitDir = Conf.desktop + "beidousplit/",
				excel="C:\\Users\\lenovo\\Documents\\Tencent Files\\329682470\\FileRecv\\beidou.xlsx",
				txt=Conf.getTranslateFile("beidou.txt");
		PacSplitter.split(Conf.pacDir, splitDir);
		Encoding enc=new Encoding();
		
		new ScriptsImporter().import_(splitDir, excel, txt, enc);
		
		VramImg fontlib=FourLayerFontGen.gen(enc.convertToGlyphs(enc.fillGlyph()), 256, 256);
		enc.saveAsTbl(Conf.outDir+"beidou-enc.tbl");
		for(int clutY:new int[]{496,497,498,499}){
			BufferedImage img=Img4bitUtil.readRomToBmp(new ByteArrayInputStream(fontlib.data), fontlib.w, fontlib.h, new Palette(16, Conf.getRawFile("clut/352-"+clutY+".16")));
			ImageIO.write(img, "bmp", new File(Conf.outDir+clutY+".bmp"));
		}
		new Fontlib().import_(splitDir, fontlib);
		
		System.out.println("rebuilding pac...");
		PacRebuilder.rebuild(Conf.pacDir, splitDir, Conf.outDir);
		IsoPatcher.patch(Conf.outDir, Conf.outDir+"beidou-hack.bin");
		System.out.println("patch finished");
	}

}
