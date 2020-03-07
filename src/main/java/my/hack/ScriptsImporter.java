package my.hack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.ExcelParser;
import common.ExcelParser.RowCallback;

public class ScriptsImporter {
	
	public void import_(String splitDir, String excel, String txt, final Encoding enc) throws IOException{
		File f=new File(excel);
		final Map<Integer,byte[]> sentences=new HashMap<>();
		final List<String> err=new ArrayList<>();
		new ExcelParser(f).parse(2, new RowCallback() {
			@Override
			public void doInRow(List<String> strs, int rowNum) {
				int maxLen=Integer.parseInt(strs.get(1));
				try {
					byte[] bs = SentenceSerializer.toBytes(enc, strs.get(3), maxLen);
					sentences.put(Integer.parseInt(strs.get(0)), bs);
				} catch (UnsupportedOperationException e) {
					int exceed=Integer.parseInt(e.getMessage());
					err.add(String.format("第%s句超出%d个字符:%s", strs.get(0), exceed/2, strs.get(3)));
				}
			}
		});
		
		if(err.size()>0){
			for(String s:err)
				System.err.println(s);
			throw new RuntimeException();
		}
		
		BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(txt)));
		String s;
		while((s=br.readLine())!=null){
			String[] kv=s.split("=");
			RandomAccessFile file=new RandomAccessFile(splitDir+kv[0], "rw");
			for(String offset_sInd : kv[1].split(";")){
				if(!"".equals(offset_sInd)){
					String[] arr=offset_sInd.split(",");
					file.seek(Integer.parseInt(arr[0],16));
					file.write(sentences.get(Integer.parseInt(arr[1])));
				}
			}
			file.close();
		}
		br.close();
	}

}
