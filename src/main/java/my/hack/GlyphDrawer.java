package my.hack;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import my.Conf;
import my.util.RscLoader;

public class GlyphDrawer {
	
	public static void main(String[] args) throws IOException {
		BufferedImage glyph=new GlyphDrawer().generateGlyph("1");
		ImageIO.write(glyph, "bmp", new File(Conf.desktop+"glyph.bmp"));
	}
	
	public GlyphDrawer(){
		this(Conf.getRawFile("simsun.ttc"),0,10);
	}
	
	Font font;
	int baseX,baseY;
	Map<String,BufferedImage> specGlyph = new HashMap<>();
	Map<String,String> replaceGlyph = new HashMap<>();
	
	public GlyphDrawer(String fontFile, int baseX, int baseY){
		try {
			InputStream is = new FileInputStream(fontFile); 
			this.font = Font.createFont(Font.TRUETYPE_FONT, is);
			this.baseX=baseX;
			this.baseY=baseY;
			is.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean canSupport(String ch){
		char[] chars=ch.toCharArray();
		return specGlyph.containsKey(ch) || 
				(chars.length==1 && font.canDisplay(chars[0])); 
	}
	
	public BufferedImage generateGlyph(String ch){
		if(specGlyph.containsKey(ch)){
			return specGlyph.get(ch);
		} else {
			if(replaceGlyph.containsKey(ch)){
				return draw(replaceGlyph.get(ch).toCharArray()[0]);
			} else {
				return draw(ch.toCharArray()[0]);
			}
		}
	}
	
	private BufferedImage draw(char c){
		int fontsize=12;
		Font myfont=font.deriveFont(Font.PLAIN, fontsize);
		
		BufferedImage img = new BufferedImage(fontsize, 16, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.setColor(Color.WHITE);	
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		g.setColor(Color.BLACK);
		g.setFont(myfont);
//		System.out.println(g.getFontMetrics(myfont).getAscent());
//		System.out.println(g.getFontMetrics(myfont).getDescent());
		g.drawString(c+"", baseX, baseY);	//向上修正N个像素,因为书写起始位置向上偏了点
		g.dispose();
		return img;
	}
	

}
