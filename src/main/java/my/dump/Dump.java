package my.dump;

import java.io.IOException;

import common.Conf;
import my.PacSplitter;
import my.pic.AllPicture;

public class Dump {
	
	public static void main(String[] args) throws IOException {
		String targetDir=Conf.desktop;
		String splitDir = Conf.desktop + "beidousplit/";
		PacSplitter.split(Conf.pacDir, splitDir);
		
		new ScriptsExporter().export(splitDir, targetDir);
		new AllPicture().export(splitDir,targetDir);
	}

}
