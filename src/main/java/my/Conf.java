package my;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Conf {
	
	public static String 
			EXE="SLPS_029.93",
			pacDir = "e:\\hanhua\\beidou\\",	//where is orginal PAC, dump from game disk
			desktop = "C:\\Users\\lenovo\\Desktop\\",
			outDir = "e:\\hanhua\\beidouhack\\";
	
	public static final int SECTOR = 0x800;
	
	public static String getRawFile(String rawFile){
		return System.getProperty("user.dir")+File.separator+"raw"+File.separator+rawFile;
	}
	
	public static String getTranslateFile(String file){
		return System.getProperty("user.dir")+File.separator+"translation"+File.separator+file;
	}
	
	
	public static int getExeOffset(int memAddr){
		return memAddr-0x8000f800;
	}
	
	public static int getExeAddr(int offset){
		return 0x8000f800+offset;
	}

}
