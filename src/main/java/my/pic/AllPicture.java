package my.pic;

import java.io.IOException;

public class AllPicture {
	
	public void export(String splitDir, String targetDir) throws IOException {
		new Fontlib().export(splitDir,targetDir);
	}

}
