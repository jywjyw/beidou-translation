package common;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Conf {
	
	public static String desktop,pacDir;
	public static final int 
	SECTOR = 0x800;
	public static String EXE="SLPS_029.93",outDir;
	
	static {
		InputStream is=null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream("conf.properties");
			Properties conf = new Properties();
			conf.load(is);
			desktop= conf.getProperty("desktop");
			pacDir = conf.getProperty("pacDir");
			outDir = conf.getProperty("outDir");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}
	
	private static void assertNotnull(Object o) {
		if(o==null)throw new RuntimeException("conf.prop初始化失败..");
	}
	
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
