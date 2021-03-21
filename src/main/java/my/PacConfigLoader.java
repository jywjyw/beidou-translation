package my;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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
	
	
	public static void loadExtractionConfig(PacConfigLoaderCallback cb){
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("pac.xml");
			Document doc = new SAXReader().read(is);
			List<Element> texts=doc.selectNodes("//text|//pic");
			for(Element e:texts){
				cb.onFoundTextOrItemElement(e.getParent().attributeValue("name"), e.attributeValue("pacItem"));
			}
			is.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public interface PacConfigLoaderCallback{
		public default void onFoundTextOrItemElement(String pac, Object...value) {}
		public default void onEveryPac(String pac, List<Integer> pacItems){}
	}
	
	/**
	 * for rebuilding pac
	 */
	public static void iteratePac4Rebuild(PacConfigLoaderCallback cb){
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("pac.xml");
			Document doc = new SAXReader().read(is);
			((List<Element>)doc.selectNodes("//pac")).forEach(pac -> {
				List<Integer> pacItems = ((List<Element>)pac.elements())
				.stream()
				.map(e -> Integer.parseInt(e.attributeValue("pacItem")))
				.collect(Collectors.toList());
				cb.onEveryPac(pac.attributeValue("name"), pacItems);
			});
			is.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public static void loadTextConfig(PacConfigLoaderCallback cb){
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("pac.xml");
			Document doc = new SAXReader().read(is);
			List<Element> texts=doc.selectNodes("//text");
			for(Element e:texts){
				cb.onFoundTextOrItemElement(e.getParent().attributeValue("name"), 
						e.attributeValue("pacItem"), e.attributeValue("key"));
			}
			is.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}







	
	
}
