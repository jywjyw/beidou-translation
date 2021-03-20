package test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Assert;
import org.junit.Test;

import my.Conf;
import my.dump.Decompressor;
import my.hack.Compressor;
import my.util.Util;

public class CompressorTest {
	
	@Test
	public void test1() throws IOException{
		File raw=new File(Conf.getRawFile("D1_1_0-6.raw"));
		FileOutputStream os=new FileOutputStream(Conf.desktop+"comp");
		new Compressor().compress(new FileInputStream(raw), os, 12);
		os.close();
	}
	
	@Test
	public void test2() throws IOException{
		File raw=new File(Conf.getRawFile("D1_1_0-6.raw"));
		ByteArrayOutputStream mycomp=new ByteArrayOutputStream();
		new Compressor().compress(new FileInputStream(raw), mycomp, 12);
		System.out.println("len after compressed:"+mycomp.size());
		
		FileOutputStream recomp=new FileOutputStream(Conf.desktop+"recomp");
		recomp.write(mycomp.toByteArray());
		recomp.close();
		
		File reuncomp=new File(Conf.desktop+"reuncomp");
		byte[] data=new Decompressor().uncompress(new ByteArrayInputStream(mycomp.toByteArray()), (int)raw.length(), 12);
		FileOutputStream reuncompOs=new FileOutputStream(reuncomp);
		reuncompOs.write(data);
		reuncompOs.close();
		
		Assert.assertEquals(Util.md5(reuncomp), Util.md5(raw));
	}
	
	@Test
	public void test3Fontlib() throws IOException{
		FileInputStream is=new FileInputStream(Conf.desktop+"beidousplit/START/8");
		ByteArrayOutputStream os=new ByteArrayOutputStream();
		new Compressor().compress(is, os, 11);
		System.out.println("compressed len="+Integer.toHexString(os.size()));
	}
	
	public static void main(String[] args) throws IOException {
//		RandomAccessFile f=new RandomAccessFile(Conf.pacDir+"D1_1_0.PAC", "r");
//		f.seek(0x4718+16);
//		byte[] data=new byte[0xdfa-16];
//		f.read(data);
//		f.close();
//		
//		FileOutputStream fos=new FileOutputStream(Conf.desktop+"D1_1_0-6.comp");
//		fos.write(data);
//		fos.close();
		RandomAccessFile f=new RandomAccessFile(Conf.desktop+"ram.bin", "r");
		f.seek(0x1c1a84);
		byte[] data=new byte[5688];
		f.read(data);
		f.close();
		
		FileOutputStream fos=new FileOutputStream(Conf.desktop+"sssss");
		fos.write(data);
		fos.close();
		System.out.println(Util.md5(new File(Conf.desktop+"sssss")));
		System.out.println(Util.md5(new File(Conf.getRawFile("D1_1_0-6.raw"))));
	}
	
}
