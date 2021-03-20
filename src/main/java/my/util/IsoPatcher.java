package my.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Map.Entry;

import my.PacConfigLoader;

import java.util.Properties;

public class IsoPatcher {
	
	private static final int 
			SECTOR_SIZE=0x930, 
			SECTOR_HEADER_SIZE=4,		//分:秒:帧+mode
			SECTOR_SUB_CHANNEL_SIZE=8;	//??
	private static final byte[] 
			SECTOR_SYNC=new byte[]{0,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,(byte)0xff,0};
	
	public static void patch(String dir, String iso) throws IOException{
		if(!new File(iso).exists()) throw new FileNotFoundException(iso+" not exists!");
		RandomAccessFile isoF=new RandomAccessFile(iso, "rw");
		for(Entry<String,Integer> e:PacConfigLoader.loadLBA().entrySet()){
			String pac=(String)e.getKey().split("/")[0];
			int lba=Integer.parseInt(e.getValue().toString());
			System.out.println("patching "+pac+"...");
			patch(dir+pac, lba, isoF);
		}
		isoF.close();
	}
	
	private static void patch(String diskItem, int lba, RandomAccessFile iso) throws IOException{
		FileInputStream item=new FileInputStream(diskItem);
		byte[] userdata=new byte[0x800];
		int len=0;
		byte[] header=new byte[SECTOR_HEADER_SIZE];
		byte[] subChannel=new byte[SECTOR_SUB_CHANNEL_SIZE];
		while((len=item.read(userdata))!=-1){
			if(len<userdata.length){
//				throw new RuntimeException(); //fill 0 to calculate edc&ecc
			}
			iso.seek(SECTOR_SIZE*lba);
			iso.skipBytes(SECTOR_SYNC.length);
			iso.read(header);
			iso.read(subChannel);
			
			byte[] edc=edc(subChannel, userdata);
			byte[] eccP=eccP(subChannel,userdata,edc);
			byte[] eccQ=eccQ(subChannel,userdata,edc,eccP);
			
			ByteBuffer sector=ByteBuffer.allocate(SECTOR_SIZE);
			sector.put(SECTOR_SYNC);
			sector.put(header);
			sector.put(subChannel);
			sector.put(userdata);
			sector.put(edc);
			sector.put(eccP);
			sector.put(eccQ);
			iso.seek(SECTOR_SIZE*lba);
			iso.write(sector.array());
			
			lba++;
		}
		item.close();
	}

	private static byte[] edc(byte[] subChannel, byte[] userData){
		byte[] bs=new byte[subChannel.length+userData.length];
		System.arraycopy(subChannel, 0, bs, 0, subChannel.length);
		System.arraycopy(userData, 0, bs, subChannel.length, userData.length);
		return SectorErrorCorrection.generateErrorDetectionAndCorrection(bs,0,bs.length);
	}
	
	private static byte[] eccP(byte[] subChannel, byte[] userData, byte[] edc) {
		ByteBuffer buf=ByteBuffer.allocate(SECTOR_HEADER_SIZE+subChannel.length+userData.length+edc.length);
		buf.put(new byte[4]);
		buf.put(subChannel);
		buf.put(userData);
		buf.put(edc);
		byte[] p=new byte[172];
		SectorErrorCorrection.generateErrorCorrectionCode_P(buf.array(), 0, p, 0);
		return p;
	}
	
	private static byte[] eccQ(byte[] subChannel, byte[] userData, byte[] edc, byte[] eccP) {
		ByteBuffer buf=ByteBuffer.allocate(SECTOR_HEADER_SIZE+subChannel.length+userData.length+edc.length+eccP.length);
		buf.put(new byte[4]);
		buf.put(subChannel);
		buf.put(userData);
		buf.put(edc);
		buf.put(eccP);
		byte[] q=new byte[104];
		SectorErrorCorrection.generateErrorCorrectionCode_Q(buf.array(), 0, q, 0);
		return q;
	}

}
