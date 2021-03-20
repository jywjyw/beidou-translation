package my.hack;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import my.Conf;
import my.hack.SentenceSplitter.Callback;

public class SentenceSerializer {
	
	public static byte[] toBytes(final Encoding enc, String sentence, int maxLen){
		final ByteBuffer buf = ByteBuffer.allocate(Conf.SECTOR);
		buf.order(ByteOrder.BIG_ENDIAN);
		SentenceSplitter.splitToWords(sentence, new Callback() {
			@Override
			public void onReadWord(boolean isCtrl, String word) {
				if("{82}".equals(word)) {
					buf.put((byte)0x82);
				}else if(word.startsWith("{85")) {
					int i=Integer.parseInt(word.replace("{", "").replace("}", ""),16);
					buf.putShort((short)(i&0xffff));
				} else {
					Integer code=enc.getCode(word);
					if(code==null){
						code=enc.put(word);
					}
					buf.putShort(code.shortValue());
				}
			}
		});
		buf.putShort((short)0xff00);
		
		int exceed=buf.position()-maxLen;
		if(exceed>0)
			throw new UnsupportedOperationException(exceed+"");
		else
			return Arrays.copyOfRange(buf.array(), 0, maxLen);
	}
	
}
