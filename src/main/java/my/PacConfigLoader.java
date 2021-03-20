package my;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class PacConfigLoader{
	
	public static Map<String,Integer> loadLBA(){
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("pac.xml");
			Element root = new SAXReader().read(is).getRootElement();
			Map<String,Integer> pac_lba=new LinkedHashMap<>();
			for(Element e : (List<Element>)root.selectNodes("//pac|//exe")){
				pac_lba.put(e.attributeValue("name"), Integer.parseInt(e.attributeValue("lba")));
			}
			return pac_lba;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public interface OnFoundTextOrItemElement{
		public void do_(String pac, Object...value);
	}
	
	
	public static void loadExtractionConfig(OnFoundTextOrItemElement cb){
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("pac.xml");
			Document doc = new SAXReader().read(is);
			List<Element> texts=doc.selectNodes("//text|//pic");
			for(Element e:texts){
				cb.do_(e.getParent().attributeValue("name"), e.attributeValue("pacItem"));
			}
			is.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void loadTextConfig(OnFoundTextOrItemElement cb){
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("pac.xml");
			Document doc = new SAXReader().read(is);
			List<Element> texts=doc.selectNodes("//text");
			for(Element e:texts){
				cb.do_(e.getParent().attributeValue("name"), 
						e.attributeValue("pacItem"), e.attributeValue("key"));
			}
			is.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
