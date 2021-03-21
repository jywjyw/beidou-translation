package my.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import my.Conf;


public class HexSearcher {
	
	public static void main(String[] args) throws IOException {
//		String dir = Conf.desktop+"beidousplit\\";
//		String dir = Conf.desktop+"raw\\";
		String dir = "e:/hanhua/beidou";
//		String dir = "e:/hanhua/beidouhack";
		System.out.println("searching...");
//		HexSearcher.searchDir(dir, "04EA08A30433092D", null);	//使用可能
//		HexSearcher.searchDir(dir, "00B200E800D100A0", null);	//na li ma si ta
//		HexSearcher.searchDir(dir, "056100a9018301820182", null);	//
		HexSearcher.searchDir(dir, "056100a90b60", null);	//
		System.out.println("finish...");
		
//		File dir=new File("D:\\hanhua\\北斗神拳editor1218\\editor\\jp-text");
//		for(File f:dir.listFiles()){
//			int len=f.getName().length();
//			System.out.println(f.getName().substring(0, len-9)+".PAC=fix");
//		}
	}
	
	public interface Callback{
		void onFound(File f, long addr);
	}
	
	public static void searchDir(String dir, String query, final Callback cb){
		final byte[] q = Util.decodeHex(query.replace(" ", ""));
		DirLooper.loop(dir, new DirLooper.Callback() {
			@Override
			public void handleFile(File f) {
//				System.out.println(f);
				search(f, q, cb);
			}
		});
	}
	
	public static void searchFile(File f, String query, Callback cb) {
		search(f, Util.decodeHex(query.replace(" ", "")), cb);
	}
	
	public static void search(File f, byte[] query, Callback cb){
		try {
			PushbackInputStream is = new PushbackInputStream(new ByteArrayInputStream(Util.loadFile(f)), query.length);
			long addr=0;
			byte[] buf = new byte[query.length];
			while(is.read(buf)==buf.length){
				if(Arrays.equals(query, buf)){
					if(cb==null){
						System.err.printf("%s : found!! %X\n", f, addr);
					} else {
						cb.onFound(f, addr);
					}
				}
				is.unread(buf, 1, buf.length-1);
				addr++;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static List<Integer> searchSingleFile(File f, byte[] query){
		try {
			PushbackInputStream is = new PushbackInputStream(new ByteArrayInputStream(Util.loadFile(f)), query.length);
			int addr=0;
			byte[] buf = new byte[query.length];
			List<Integer> ret = new ArrayList<>();
			while(is.read(buf)==buf.length){
				if(Arrays.equals(query, buf)){
					ret.add(addr);
				}
				is.unread(buf, 1, buf.length-1);
				addr++;
			}
			return ret;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
