package my.dump;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import my.PacConfigLoader;
import my.PacConfigLoader.PacConfigLoaderCallback;
import my.util.Util;

public class ScriptsExporter {
	
	Charset cs=new Charset();
	Map<String,Sentence> sentences=new LinkedHashMap<>();
	
	public void export(final String splitDir, String targetDir)throws IOException {
		exportPacs(splitDir);
		
		Collection<Sentence> slist=sentences.values();
		writeXls(slist,targetDir);
		writeTxt(slist,targetDir);
	}
	
	private void exportPacs(final String splitDir)throws IOException{
		PacConfigLoader.loadTextConfig(new PacConfigLoaderCallback() {
			@Override
			public void onFoundTextOrItemElement(final String pac, final Object... value) {
				try {
					final String pacName=pac.replace(".PAC", "");
					RandomAccessFile f=new RandomAccessFile(splitDir+pacName+"/"+value[0], "r");
					for(Entry<Integer,Integer> pointerArea : readPointerAreas(pacName, f, Integer.parseInt(value[1]+"",16)).entrySet()){
						f.seek(pointerArea.getKey());
						for(int p : readPointers(f, pointerArea.getValue())){
							f.seek(pointerArea.getKey()+p);
							new ScriptReader().readUtilEOF(f, cs, new ScriptReader.Callback(){
								@Override
								public boolean onReadedSentence(String s, int startAddr, int len, long filePos) {
									Addr addr=new Addr(pacName+"/"+value[0], startAddr);
									Sentence exist=sentences.get(s);
									if(exist==null){
										exist=new Sentence(s, len);
										exist.addrs.add(addr);
										sentences.put(s, exist);
									}else{
										exist.addrs.add(addr);
									}
									return false;
								}
							});
						}
					}
					f.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 
	 * @param pac
	 * @param f
	 * @param keyAddr
	 * @return multiple pointerAreas. k=offset, v=size
	 * @throws IOException
	 */
	private Map<Integer,Integer> readPointerAreas(String pac, RandomAccessFile f, int keyAddr) throws IOException{
		if(pac.equals("D_OP00")) 
			return Collections.singletonMap(0,0x20a);	//v=pac bytes length
		else if(pac.equals("SCENE") && keyAddr==0) 
			return Collections.singletonMap(0, 0x19fa);
		else if(pac.contains("STAGE")||pac.contains("TITLE")||pac.contains("TITLE2"))
			return Collections.singletonMap(0, 0xf9a);
		
		Map<Integer,Integer> ret=new LinkedHashMap<>();
		f.seek(keyAddr);
		while(true){
			int offset = Util.hilo(f.readInt()), 
				blockBytes=Util.hilo(f.readInt())+2;	//block=unk(2Bytes)+pointerCount(2Bytes)+pointers+text
			if(offset==0) {
				break;
			}else {
				ret.put(offset, blockBytes);
			}
		}
		return ret;
	}
	
	/**
	 * 
	 * @param f
	 * @param size
	 * @return pointers. 
	 * @throws IOException
	 */
	private Set<Integer> readPointers(RandomAccessFile f, int size) throws IOException{
		short unk=f.readShort();	//每个指针区块前都有4个字节,前2个字节未知,后2个字节代表有多少个指针
		short pointerCount = Util.hiloShort(f.readShort());
		Set<Integer> ret=new LinkedHashSet<>();
		for(int i=0;i<pointerCount;i++){
			int p=Util.hiloShort(f.readShort())&0xffff;
			if(p<size-2) {
				ret.add(p);
			}
		}
		return ret;
	}
	
 
	
	private static void writeXls(Collection<Sentence> sentences, String targetDir) throws IOException{
		XSSFWorkbook xls=new XSSFWorkbook();
		XSSFSheet sheet=xls.createSheet();
		int rownum=0;
		Row r0=sheet.createRow(rownum++);
		r0.createCell(0).setCellValue("语句编号");
		r0.createCell(1).setCellValue("最大长度");
		r0.createCell(2).setCellValue("日文");
		r0.createCell(3).setCellValue("中文");
		int sInd=1;
		for(Sentence s:sentences){
			Row r=sheet.createRow(rownum++);
			s.index=sInd++;
			r.createCell(0).setCellValue(s.index);
			r.createCell(1).setCellValue(s.len);
			r.createCell(2).setCellValue(s.text);
		}
		FileOutputStream os=new FileOutputStream(targetDir+"beidou.xlsx");
		xls.write(os);
		xls.close();
		os.flush();
		os.close();
	}
	
	private void writeTxt(Collection<Sentence> sentences, String targetDir) throws FileNotFoundException {
		Map<String,Map<Integer,Integer>> pac_addr=new TreeMap<>();
		for(Sentence s:sentences){
			for(Addr a:s.addrs){
				Map<Integer,Integer> exist=pac_addr.get(a.file);
				if(exist==null){
					exist=new TreeMap<>();
					exist.put(a.offset, s.index);
					pac_addr.put(a.file, exist);
				}else{
					exist.put(a.offset, s.index);
				}
			}
		}
		
		PrintWriter pw=new PrintWriter(new OutputStreamWriter(new FileOutputStream(targetDir+"beidou.idx")));
		for(Entry<String,Map<Integer,Integer>> e:pac_addr.entrySet()){
			pw.write(e.getKey());
			pw.write("=");
			for(Entry<Integer,Integer> ee:e.getValue().entrySet()){
				pw.write(Integer.toHexString(ee.getKey()));
				pw.write(",");
				pw.write(ee.getValue()+"");
				pw.write(";");
			}
			pw.println();
		}
		pw.flush();
		pw.close();
		
	}

}
