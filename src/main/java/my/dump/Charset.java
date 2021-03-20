package my.dump;

import java.util.HashMap;
import java.util.Map;

import my.util.RscLoader;
import my.util.RscLoader.Callback;

public class Charset {
	Map<Integer,String> code_char = new HashMap<>();
	Map<String,Integer> char_code = new HashMap<>();
	
	public Charset() {
		RscLoader.load("charset.gbk", "gbk", new Callback() {
			@Override
			public void doInline(String line) {
				String[] arr = line.split("=",2);
				int code = Integer.parseInt(arr[0], 16);
				code_char.put(code,arr[1]);
				char_code.put(arr[1], code);
			}
		});
	}
	
	public String getChar(int code) {
		return code_char.get(code);
	}
	
	public boolean containChar(int code) {
		return code_char.containsKey(code);
	}
	
	public Integer getCode(String char_) {
		return char_code.get(char_);
	}
	
}
