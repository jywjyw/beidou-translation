package my;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import my.dump.Decompressor;
import my.util.Util;

public class PacItem {
	
	public int unk,uncompLen,posBits,zero;
	public byte[] head=new byte[16], data;
	
	public static PacItem decompress(RandomAccessFile file, int offset, int len) throws IOException{
		PacItem i=new PacItem();
		file.seek(offset);
		
		file.read(i.head);
		DataInputStream dis=new DataInputStream(new ByteArrayInputStream(i.head));
		i.unk=dis.readInt();	//TODO
		i.uncompLen=Util.hilo(dis.readInt());
		i.posBits=Util.hilo(dis.readInt())&0xffff;
		i.zero=dis.readInt();//TODO unk, maybe is fixed to 0
		
		i.data = new Decompressor().uncompress(new InputStreamAdapter(file), i.uncompLen, i.posBits);
		return i;
	}
	

}
