/**
 * Distribution License:
 * JSword is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, version 2.1 or later
 * as published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * The License is available on the internet at:
 *      http://www.gnu.org/copyleft/lgpl.html
 * or by writing to:
 *      Free Software Foundation, Inc.
 *      59 Temple Place - Suite 330
 *      Boston, MA 02111-1307, USA
 *
 * © CrossWire Bible Society, 2007 - 2016
 *
 */
package my.hack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import my.util.Util;

public class Compressor  {
	
	private static final int THRESHOLD = 3;
    private byte[] ringBuffer;
    private short maxStoreLen,ringSize,ringWrap,notUsed;
    private short matchPos,matchLen;

    public void compress(InputStream input, OutputStream out, int posBits) throws IOException {
    	maxStoreLen = (short) ((1 << (16-posBits))-1 + THRESHOLD);
    	ringSize = (short) (1<<posBits);
    	ringWrap = (short) (ringSize - 1);
		notUsed = ringSize;
		int posMask=(1<<(posBits-8))-1;
    			
    	ringBuffer = new byte[ringSize + maxStoreLen - 1];
    	dad = new short[ringSize + 1];
    	leftSon = new short[ringSize + 1];
    	rightSon = new short[ringSize + 257];
    	
        short i; // an iterator
        int readResult;
//        short r = (short) (ringSize - maxStoreLen); // node number in the binary tree
//        short s = 0; // position in the ring buffer
        short r=1;
        short s=(short) (maxStoreLen+r);
        
        short len=0; // length of initial string
        short lastMatchLength; // length of last match
        
        Flag flag=new Flag();
        byte[] codeBuff = new byte[flag.capacity*2]; // the output buffer
        short codeBufPos = 0; // position in the output buffer
        byte c; // character read from string

        // Start with a clean tree.
        initTree();

//        Arrays.fill(ringBuffer, 0, r, (byte) ' ');

        readResult = input.read(ringBuffer, r, maxStoreLen);
        if (readResult <= 0) {
            return;
        }
        len = (short)readResult;

        // Insert the MAX_STORE_LENGTH strings, each of which begins with one or more
        // 'space' characters. Note the order in which these strings
        // are inserted. This way, degenerate trees will be less likely
        // to occur.
//        for (i = 1; i <= maxStoreLen; i++) {
//            insertNode((short)(r - i));
//        }

        // Finally, insert the whole string just read. The member variables matchLength and matchPosition are set.
        insertNode(r);
        
        // Now that we're preloaded, continue till done.
        do {
            // matchLength may be spuriously long near the end of text.
            if (matchLen > len) {
                matchLen = len;
            }
            flag.next();
            if (matchLen < THRESHOLD) {
                matchLen = 1;
                codeBuff[codeBufPos++] = ringBuffer[r];
                flag.indicate(false);
//                System.out.printf("%x\n", ringBuffer[r]);
            } else {
            	flag.indicate(true);
            	int offset=r-matchPos-1;
                codeBuff[codeBufPos++] = (byte) offset;
                codeBuff[codeBufPos++] = (byte) ((matchLen - THRESHOLD)<<(posBits-8) | ((offset>>8) & posMask));
//                System.out.printf("(%d,%d)\n", offset, matchLen);
            }
            if (flag.isFull()) {
            	out.write(flag.build());
                out.write(codeBuff, 0, codeBufPos);
                // Reset for next buffer...
                codeBufPos = 0;
                flag=new Flag();
            }

            lastMatchLength = matchLen;

            // Delete old strings and read new bytes...
            for (i = 0; i < lastMatchLength; i++) {
                readResult = input.read();
                if (readResult == -1) {
                    break;
                }
                c = (byte) readResult;

                deleteNode(s);
                ringBuffer[s] = c;
                if (s < maxStoreLen - 1) {
                    ringBuffer[s + ringSize] = c;
                }

                s = (short) ((s + 1) & ringWrap);
                r = (short) ((r + 1) & ringWrap);

                // Register the string that is found in
                // ringBuffer[r..r + MAX_STORE_LENGTH - 1].
                insertNode(r);
            }

            // If we didn't quit because we hit the lastMatchLength,
            // then we must have quit because we ran out of characters
            // to process.
            while (i++ < lastMatchLength) {
                deleteNode(s);
                s = (short) ((s + 1) & ringWrap);
                r = (short) ((r + 1) & ringWrap);

                // Note that len hitting 0 is the key that causes the
                // do...while() to terminate. This is the only place
                // within the loop that len is modified.
                //
                // Its original value is MAX_STORE_LENGTH (or a number less than
                // MAX_STORE_LENGTH for
                // short strings).
                if (--len != 0) {
                    insertNode(r); /* buffer may not be empty. */
                }
            }
            // End of do...while() loop. Continue processing until there
            // are no more characters to be compressed. The variable
            // "len" is used to signal this condition.
        } while (len > 0);
        // There could still be something in the output buffer. Send it now.
        if (codeBufPos > 0) {
        	out.write(flag.build());
            out.write(codeBuff, 0, codeBufPos);
        }
    }
    
    private void initTree() {
        Arrays.fill(dad, 0, dad.length, notUsed);
        Arrays.fill(leftSon, 0, leftSon.length, notUsed);
        Arrays.fill(rightSon, 0, rightSon.length, notUsed);
    }

    private void insertNode(short pos) {
        int cmp = 1;
        short key = pos;
        // The last 256 entries in rightSon contain the root nodes for
        // strings that begin with a letter. Get an index for the
        // first letter in this string.
        short p = (short) (ringSize + 1 + (ringBuffer[key] & 0xFF));
        if(pos<0||pos>=ringSize||p<=ringSize) throw new RuntimeException();

        // Set the left and right tree nodes for this position to "not used."
        leftSon[pos] = notUsed;
        rightSon[pos] = notUsed;

        // Haven't matched anything yet.
        matchLen = 0;

        while (true) {
            if (cmp >= 0) {
                if (rightSon[p] != notUsed) {
                    p = rightSon[p];
                } else {
                    rightSon[p] = pos;
                    dad[pos] = p;
                    return;
                }
            } else {
                if (leftSon[p] != notUsed) {
                    p = leftSon[p];
                } else {
                    leftSon[p] = pos;
                    dad[pos] = p;
                    return;
                }
            }
            
            if(p==0) break;//??

            // Should we go to the right or the left to look for the
            // next match?
            short i = 0;
            for (i = 1; i < maxStoreLen; i++) {
                cmp = (ringBuffer[key + i] & 0xFF) - (ringBuffer[p + i] & 0xFF);
                if (cmp != 0) {
                    break;
                }
            }

            if (i > matchLen) {
                matchPos = p;
                matchLen = i;

                if (i >= maxStoreLen) {
                    break;
                }
            }
        }

        dad[pos] = dad[p];
        leftSon[pos] = leftSon[p];
        rightSon[pos] = rightSon[p];

        dad[leftSon[p]] = pos;
        dad[rightSon[p]] = pos;

        if (rightSon[dad[p]] == p) {
            rightSon[dad[p]] = pos;
        } else {
            leftSon[dad[p]] = pos;
        }

        // Remove "p"
        dad[p] = notUsed;
    }

    /**
     * Remove a node from the tree.
     * 
     * @param node
     *            the node to remove
     */
    private void deleteNode(short node) {
        assert node >= 0 && node < (ringSize + 1);

        short q;

        if (dad[node] == notUsed) {
            // not in tree, nothing to do
            return;
        }

        if (rightSon[node] == notUsed) {
            q = leftSon[node];
        } else if (leftSon[node] == notUsed) {
            q = rightSon[node];
        } else {
            q = leftSon[node];
            if (rightSon[q] != notUsed) {
                do {
                    q = rightSon[q];
                } while (rightSon[q] != notUsed);

                rightSon[dad[q]] = leftSon[q];
                dad[leftSon[q]] = dad[q];
                leftSon[q] = leftSon[node];
                dad[leftSon[node]] = q;
            }

            rightSon[q] = rightSon[node];
            dad[rightSon[node]] = q;
        }

        dad[q] = dad[node];

        if (rightSon[dad[node]] == node) {
            rightSon[dad[node]] = q;
        } else {
            leftSon[dad[node]] = q;
        }

        dad[node] = notUsed;
    }

    /**
     * leftSon, rightSon, and dad are the Japanese way of referring to a tree
     * structure. The dad is the parent and it has a right and left son (child).
     * 
     * <p>
     * For i = 0 to RING_SIZE-1, rightSon[i] and leftSon[i] will be the right
     * and left children of node i.
     * </p>
     * 
     * <p>
     * For i = 0 to RING_SIZE-1, dad[i] is the parent of node i.
     * </p>
     * 
     * <p>
     * For i = 0 to 255, rightSon[RING_SIZE + i + 1] is the root of the tree for
     * strings that begin with the character i. Note that this requires one byte
     * characters.
     * </p>
     * 
     * <p>
     * These nodes store values of 0...(RING_SIZE-1). Memory requirements can be
     * reduces by using 2-byte integers instead of full 4-byte integers (for
     * 32-bit applications). Therefore, these are defined as "shorts."
     * </p>
     */
    private short[] dad;
    private short[] leftSon;
    private short[] rightSon;
    
    private class Flag{
    	private int flag=0x80000000, capacity=16;
    	public void next(){
    		this.flag >>>=1;
    	}
    	
    	public void indicate(boolean isComp){
    		if(isComp) this.flag |= 0x80000000;
    	}
    	
    	public boolean isFull(){
    		return (flag&0x8000)==0x8000;
    	}
    	
    	public byte[] build(){	//对齐到16个
    		while(!isFull()){
    			next();
    		}
    		return new byte[]{(byte)(flag>>16), (byte)(flag>>>24)};
    	}
    }

}
