import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HuffDecompression implements IHuffConstants {

	private int[] myCount;
	private int writtenBits;

	public HuffDecompression() {
		this.myCount = new int[ALPH_SIZE];
	}

	public void doDecompression(InputStream in, OutputStream out, IHuffViewer viewer) throws IOException {
		BitInputStream bitInputStream = new BitInputStream(new BufferedInputStream(in));
		BitOutputStream bitOutputStream = new BitOutputStream(new BufferedOutputStream(out));
		int magicNum = bitInputStream.readBits(BITS_PER_INT); // Step 1
		// decompression does not occur if bitinputstream not starting with magic num
		if (magicNum != MAGIC_NUMBER) {
			// viewer.showError("Error reading compressed file. \n" + "File did not start
			// with the huff magic number.");
		} else {
			// next bits read in to determine header
			int headerFormat = bitInputStream.readBits(BITS_PER_INT); // Step 2
			if (headerFormat == STORE_COUNTS) {
				storeCountsFormat(bitInputStream, bitOutputStream);
			} else if (headerFormat == STORE_TREE) {
				storeTreeFormat(bitInputStream, bitOutputStream);
			}
		}
		// flush
		bitInputStream.close();
		bitOutputStream.close();
	}

	// SCF decompression
	private void storeCountsFormat(BitInputStream bitInputStream, BitOutputStream bitOutputStream) throws IOException {
		int chunks = 0;
		while (chunks != ALPH_SIZE) {
			int freq = bitInputStream.readBits(BITS_PER_INT); // Step 3
			myCount[chunks] += freq;
			chunks++;
		}
		HuffPriorityQueue<TreeNode> huffPriorityQueue = new HuffPriorityQueue<>();
		fillPriorityQueue(huffPriorityQueue);
		HuffmanCodeTree huffmanCodeTree = new HuffmanCodeTree(huffPriorityQueue);
		decode(bitInputStream, bitOutputStream, huffmanCodeTree); // Step 4,5
	}

	private void storeTreeFormat(BitInputStream bitInputStream, BitOutputStream bitOutputStream) throws IOException {
		final int DUMMY_VAL = -1;
		final int TREE_VAL = BITS_PER_WORD + 1;
		HuffmanCodeTree huffmanCodeTree = new HuffmanCodeTree();
		int treeRepresentationBitSize = bitInputStream.readBits(BITS_PER_INT);
		huffmanCodeTree.stfDecode(bitInputStream.readBits(1), DUMMY_VAL); // Read the first val for root
		int count = 1;
		while (count != treeRepresentationBitSize) {
			int bit = bitInputStream.readBits(1);
			count++;
			if (bit == 1) {
				huffmanCodeTree.stfDecode(bit, bitInputStream.readBits(TREE_VAL));
				count += TREE_VAL; // We read in extra vals
			} else {
				huffmanCodeTree.stfDecode(bit, DUMMY_VAL);
			}
		}
		decode(bitInputStream, bitOutputStream, huffmanCodeTree);
	}

	// Fill the queue with all the chunks
	private void fillPriorityQueue(HuffPriorityQueue<TreeNode> huffPriorityQueue) {
		for (int chunk = 0; chunk < myCount.length; chunk++) {
			if (myCount[chunk] > 0) {
				huffPriorityQueue.enqueue(new TreeNode(chunk, myCount[chunk]));
			}
		}
		huffPriorityQueue.enqueue(new TreeNode(PSEUDO_EOF, 1));
	}

	private void decode(BitInputStream bits, BitOutputStream bios, HuffmanCodeTree huffmanCodeTree) throws IOException {
		final int PEOF = -2;
		final int INTERNAL_NODE = -1;
		final int NOT_ENOUGH_BITS = -1;
		boolean done = false;
		while (!done) {
			int bit = bits.readBits(1);
			// makes sure there is a pseudo val in the compressed file in order to
			// uncompress
			if (bit == NOT_ENOUGH_BITS) {
				bits.close();
				bios.close();
				throw new IOException(
						"Error reading compressed file. \n" + "unexpected end of input. No PSEUDO_EOF value.");
			} else {
				int val = huffmanCodeTree.uncompress(bit);
				// done decompressing when pseudo value reached
				if (val == PEOF) {
					done = true;
				} else if (val != INTERNAL_NODE) {
					bios.writeBits(BITS_PER_WORD, val);
					writtenBits += BITS_PER_WORD;
				}
			}
		}
	}

	public int writtenBits() {
		return this.writtenBits;
	}

}
