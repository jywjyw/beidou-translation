package my.hack;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import common.RscLoader;
import common.RscLoader.Callback;

public class Encoding {
	private int row=16;	//original=16,target=21
	private int charCapacity=4*row*21;
	private Map<String,Integer> 
		char_code=new LinkedHashMap<>();
	
	private LinkedList<Integer> codePool=new LinkedList<>();
	
	//字库图层叠顺序:0c00,0800,0400,0000
	private int[] bases=new int[]{0x0000,0x0400,0x0800,0x0c00};
	public Encoding(){
		for(int base:bases){
			for(int y=0;y<row;y++){
				for(int x=0;x<21;x++){
					codePool.add(base+(y*0x20)+x);
				}
			}
		}
		RscLoader.load("init_enc.gbk", "gbk", new Callback() {
			@Override
			public void doInline(String line) {
				String[] arr=line.split("=");
				String ch=arr[1];
				int code=Integer.parseInt(arr[0],16);
				if(codePool.contains(code)){
					codePool.remove(new Integer(code));
					char_code.put(ch, code);
				}else{
					char_code.put(ch, code);
					charCapacity++;
				}
			}
		});
 	}
	
	public Integer getCode(String char_){
		return char_code.get(char_);
	}
	
	public int put(String char_){
		Integer code=codePool.poll();
		if(code==null) 
			throw new FontlibException("too many char, only support char count:"+charCapacity);
		char_code.put(char_,code);
		return code;
	}
	
	public int size(){
		return char_code.size();
	}
	
	private Map<Integer,Char> buildSortedKv(){
		List<Kv> kvs=new ArrayList<>();
		for(Entry<String,Integer> e:char_code.entrySet()){
			kvs.add(new Kv(e.getKey(),e.getValue()));
		}
		Collections.sort(kvs, new Comparator<Kv>() {
			@Override
			public int compare(Kv o1, Kv o2) {
				return o1.code-o2.code;
			}
		});
		Map<Integer,Char> ret=new LinkedHashMap<>();
		for(Kv k:kvs){
			ret.put(k.code, new Char(k.char_));
		}
		return ret;
	}
	
	public Map<Integer,Char> fillGlyph() throws IOException{
		System.out.printf("字库使用数=%d,总数=%d\n",size(),charCapacity);
		fillGBK();	//fill to max
		Map<Integer,Char> map = buildSortedKv();
		GlyphDrawer glyphDrawer = new GlyphDrawer();
		Set<String> unsupport=new HashSet<>();
		for(Entry<Integer,Char> e:map.entrySet()){
			if(glyphDrawer.canSupport(e.getValue().ch)){
				e.getValue().glyph = glyphDrawer.generateGlyph(e.getValue().ch);
			} else {
				unsupport.add(e.getValue().ch);
			}
		}
		if(!unsupport.isEmpty()){
			throw new UnsupportedOperationException("字库不支持以下字符: "+Arrays.deepToString(unsupport.toArray()));
		}
		return map;
	}
	
	public List<List<BufferedImage>> convertToGlyphs(Map<Integer,Char> chars){
		List<List<BufferedImage>> ret = new ArrayList<>(4);
		for(int i=0;i<4;i++){
			ret.add(new ArrayList<BufferedImage>());
		}
		int page=0,charsPerPage=0;
		for(Entry<Integer,Char> e:chars.entrySet()){
			ret.get(page).add(e.getValue().glyph);
			charsPerPage++;
			if(charsPerPage==row*21) {
				charsPerPage=0;
				page++;
			}
		}
		Collections.reverse(ret);
		return ret;
	}
	
	private void fillGBK(){
		for(int a=0xb0;a<=0xf7;a++){
			for(int b=0xa1;b<=0xfe;b++){
				if(size()>=charCapacity) break;
				try {
					String zh = new String(new byte[]{(byte)a,(byte)b},"gbk");
					if(getCode(zh)==null)	put(zh);
				} catch (UnsupportedEncodingException e) {}
			}
		}
	}
	
	public void saveAsTbl(String outFile){
		Map<Integer,Char> sorted = buildSortedKv();
		try {
			OutputStreamWriter fos = new OutputStreamWriter(new FileOutputStream(outFile),"gbk");
			for(Entry<Integer,Char> e:sorted.entrySet()){
				fos.write(String.format("%04X=%s\n", e.getKey(),e.getValue().ch));
			}
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class Kv{
		public String char_;
		public int code;
		public Kv(String char_, int code) {
			this.char_ = char_;
			this.code = code;
		}
	}

}
