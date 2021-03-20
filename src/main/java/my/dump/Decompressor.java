package my.dump;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import my.Conf;

public class Decompressor {
	
	public static void main(String[] args) throws IOException {
		FileInputStream is=new FileInputStream(Conf.getRawFile("D1_1_0-6.comp"));
		
		FileOutputStream fos=new FileOutputStream(Conf.desktop+"ddddd");
		fos.write(new Decompressor().uncompress(is, 5688, 12));
		fos.close();
		
		is.close();
		
		System.out.println("finish");
	}
	
	/**
	 * 
	 * @param in
	 * @param uncompLen
	 * @param posBits 11=for pic,  12=for text
	 * @return
	 * @throws IOException
	 */
    public byte[] uncompress(InputStream in, Integer uncompLen, int posBits) throws IOException {
    	int threshold=3, 
    		lenBits=16-posBits,
    		posMask=(1<<posBits)-1;		//pos掩码,也代表pos最大值
    	
    	int readLen=0;
    	BytesStream out=new BytesStream();
        int flags=0, flagCount=0; 
        byte[] tuple = new byte[2];
        int backOffset,len;
        
        try {
	        while (true) {
	        	if(uncompLen!=null && out.size()>=uncompLen) break;
	        	if(flagCount > 0) {
	        		flags >>>= 1;
	                flagCount--;
	            } else {
	            	try {
	            		int a=in.read(),b=in.read();
	            		flags = b<<8|a;	// Next 2 bytes must be a flag.
					} catch (EOFException e) {
						break;
					}
	            	readLen+=2;
	            	flagCount = 15;	// 16 bits of flags
	            }
	        	
	            if ((flags&1) == 0) {	//输出原始字节
	            	int raw=in.read();
	            	readLen++;
//	            	System.out.printf("outPos=%x,raw=%02x\n",out.size(),raw);
	                out.write(raw);
	            } else {	//输出二元组
					int readSize=in.read(tuple);	// size must equals 2 or -1
					readLen+=2;
					if(readSize==-1){
						System.out.println("EOF, break");
					}else if(readSize!=2){
						throw new RuntimeException("read size !=2");
					}
					backOffset=(((tuple[1]&0xff)<<8)|tuple[0]&0xff) & posMask;
					len=((tuple[1]&0xff)>>>(8-lenBits))+threshold;
//					System.out.printf("outPos=%x,tuple=%02x%02x,offset=%d,len=%d\n",out.size(),tuple[0],tuple[1],backOffset,len);
					for(int i=0;i<len;i++){
						byte b=out.get(backOffset+1);
						out.write(b);
					}
	            }
	        }
        } catch(Exception e){
        	e.printStackTrace();
        	throw new RuntimeException(e);
        }
        return out.toByteArray();
    }
}
