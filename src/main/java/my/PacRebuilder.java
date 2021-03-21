package my;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

import my.PacConfigLoader.PacConfigLoaderCallback;
import my.hack.Compressor;
import my.util.Util;

public class PacRebuilder {

	public static void rebuild(final String pacDir, final String splitDir, final String dstDir) {
		PacConfigLoader.iteratePac4Rebuild(new PacConfigLoaderCallback() {
			
			@Override
			public void onEveryPac(String pac, List<Integer> pacItems)  {
				try {
					if (pac.contains("/")) {
						handleWithChildDir(pac, pacItems);
					} else {
						handleNoChildDir(pac, pacItems);
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			private void handleWithChildDir(String pac, List<Integer> pacItems) throws IOException {
				if(pacItems.size()>1)
					throw new UnsupportedOperationException("assert stage.pac,title.pac,title2.pac have only one text area");
				String[] arr = pac.split("/");
				pac = arr[0];
				int childPacIndex = Integer.parseInt(arr[1]);
				int grandsonIndex = pacItems.get(0);
				
				Util.copyFile(pacDir + pac, dstDir + pac);
				RandomAccessFile pacF = new RandomAccessFile(dstDir + pac, "rw");
				pacF.seek(8 * childPacIndex); // 寻找子pac的指针位置
				int childPacOffset = Util.hilo(pacF.readInt());
				int childPacLen = Util.hilo(pacF.readInt());

				pacF.seek(childPacOffset);
				pacF.skipBytes(8 * grandsonIndex);
				int grandsonOffset = Util.hilo(pacF.readInt());
				int grandsonLen = Util.hilo(pacF.readInt());
				
				String itemName = splitDir + pac.replace(".PAC", "") + File.separator + childPacIndex + File.separator + grandsonIndex;
				
				RandomAccessFile patchHead = new RandomAccessFile(itemName + ".head", "r");
				patchHead.seek(8);
				int posBits = patchHead.readByte();
				patchHead.close();

				FileInputStream patch = new FileInputStream(itemName);
				ByteArrayOutputStream compPatch = new ByteArrayOutputStream();
				new Compressor().compress(patch, compPatch, posBits);
				patch.close();

				if (compPatch.size() > grandsonLen) {
					String err = pac + "压缩后比原pac要大" + (compPatch.size() - grandsonLen)
							+ "字节,处理方法:根据translation/beidou.idx找出该pac中的所有文本,精简译文";
					System.err.println("pac rebuild failed: " + err);
				} else {
					pacF.seek(childPacOffset + 16);
					pacF.skipBytes(grandsonOffset);
					pacF.write(compPatch.toByteArray());
				}
				pacF.close();
			}
			
			
			//scene.pac has multiple text areas
			private void handleNoChildDir(String pac, List<Integer> pacItems) throws IOException {
				Util.copyFile(pacDir + pac, dstDir + pac);
				RandomAccessFile pacF = new RandomAccessFile(dstDir + pac, "rw");
				
				for(int pacItem : pacItems) {
					pacF.seek(8 * pacItem);
					int offset = Util.hilo(pacF.readInt());
					int len = Util.hilo(pacF.readInt());
					
					String itemName = splitDir + pac.replace(".PAC", "") + File.separator + pacItem;
					
					RandomAccessFile patchHead = new RandomAccessFile(itemName + ".head", "r");
					patchHead.seek(8);
					int posBits = patchHead.readByte();
					patchHead.close();

					FileInputStream patch = new FileInputStream(itemName);
					ByteArrayOutputStream compPatch = new ByteArrayOutputStream();
					new Compressor().compress(patch, compPatch, posBits);
					patch.close();
					if (compPatch.size() > len) {
						String err = pac + "压缩后比原pac要大" + (compPatch.size() - len)
								+ "字节,处理方法:根据translation/beidou.idx找出该pac中的所有文本,精简译文";
						System.err.println("pac rebuild failed: " + err);
					} else {
						pacF.seek(offset + 16);
						pacF.write(compPatch.toByteArray());
					}
				}
				pacF.close();
			}
		});
	}
}
