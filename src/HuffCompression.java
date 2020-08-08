import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class HuffCompression implements IHuffConstants {
	private int headerFormat;
	private int originalSize;
	private int newSize;
	private int[] myCount; // Freq of each "BITS_PER_WORD" chunk
	private HuffmanCodeTree huffmanCodeTree;
	private Map<Integer, String> encodedBits;

	// Constructor
	public HuffCompression(int headerFormat) {
		this.headerFormat = headerFormat;
		this.myCount = new int[ALPH_SIZE];
	}

	public void doCompression(InputStream in, OutputStream out, boolean force, IHuffViewer viewer) throws IOException {
		int difference = this.getNewSize() - this.getOldSize();
		if (!force && difference > 0) {
			// viewer.showError("Compressed file has " + difference + " more bits than
			// uncompressed file.\n"
			// + "Select 'Force Compression' option to compress.");
		} else {
			BitInputStream bitInputStream = new BitInputStream(new BufferedInputStream(in));
			BitOutputStream bitOutputStream = new BitOutputStream(new BufferedOutputStream(out));
			bitOutputStream.writeBits(BITS_PER_INT, MAGIC_NUMBER); // Step 1
			if (headerFormat == STORE_COUNTS) {
				storeCountsFormat(bitOutputStream);
			} else if (headerFormat == STORE_TREE) {
				storeTreeFormat(bitOutputStream);
			}
			compressingData(bitInputStream, bitOutputStream);
			bitInputStream.close();
			writePseudo(bitOutputStream);
			bitOutputStream.close();
		}
	}

	public void doPreprocess(InputStream in) throws IOException {
		BitInputStream bitInputStream = new BitInputStream(new BufferedInputStream(in));
		countBitFreqOriginalFile(myCount, bitInputStream);
		HuffPriorityQueue<TreeNode> huffPriorityQueue = new HuffPriorityQueue<>();
		fillPriorityQueue(huffPriorityQueue);
		this.huffmanCodeTree = new HuffmanCodeTree(huffPriorityQueue);
		this.encodedBits = huffmanCodeTree.encodedBits();
		// map of new encodings and frequencies
		this.newSize = BITS_PER_INT + BITS_PER_INT; // Step 1 (Magic num) + Step 2 (Header Format)
		countHeaderData(); // Step 3 (Count the size of the header info)
		countCompressedDataSize(encodedBits); // Step 4
		this.newSize += encodedBits.get(PSEUDO_EOF).length(); // Step 5
		bitInputStream.close(); // check flush
	}

	/*
	 * Compress Helper Methods
	 */

	private void storeCountsFormat(BitOutputStream bitOutputStream) {
		bitOutputStream.writeBits(BITS_PER_INT, STORE_COUNTS); // Step 2.1
		for (int i = 0; i < ALPH_SIZE; i++) {
			bitOutputStream.writeBits(BITS_PER_INT, myCount[i]); // Step 3.1
		}
	}

	private void storeTreeFormat(BitOutputStream bitOutputStream) {
		bitOutputStream.writeBits(BITS_PER_INT, STORE_TREE); // Step 2.2
		String bitTree = huffmanCodeTree.preOrderTraversal(); // Step 3.2
		bitOutputStream.writeBits(BITS_PER_INT, bitTree.length());
		for (int i = 0; i < bitTree.length(); i++) {
			int encodedBit = Integer.parseInt(bitTree.charAt(i) + "");
			bitOutputStream.writeBits(1, encodedBit);
		}
	}

	private void compressingData(BitInputStream bitInputStream, BitOutputStream bitOutputStream) throws IOException {
		final int NOT_ENOUGH_BITS = -1;
		int inputBits = bitInputStream.readBits(BITS_PER_WORD);
		while (inputBits != NOT_ENOUGH_BITS) {
			String encoded = encodedBits.get(inputBits);
			int encodedInt = Integer.valueOf(encoded, 2);
			bitOutputStream.writeBits(encoded.length(), encodedInt); // Step 4
			inputBits = bitInputStream.readBits(BITS_PER_WORD);
		}
	}

	private void writePseudo(BitOutputStream bitOutputStream) {
		String pseudoEOF = encodedBits.get(PSEUDO_EOF);
		int pseudoEOFvalue = Integer.valueOf(pseudoEOF, 2);
		bitOutputStream.writeBits(pseudoEOF.length(), pseudoEOFvalue); // Step 5
	}

	/*
	 * Preprocess Helper Methods
	 */

	// Counting frequencies of each "BITS_PER_WORD" chunk in original file
	private void countBitFreqOriginalFile(int[] myCount, BitInputStream bitInputStream) throws IOException {
		final int NOT_ENOUGH_BITS = -1;
		int inbits = 0;
		while ((inbits = bitInputStream.readBits(IHuffConstants.BITS_PER_WORD)) != NOT_ENOUGH_BITS) {
			myCount[inbits]++;
			originalSize += BITS_PER_WORD;
		}
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

	private void countHeaderData() {
		final int TREE_REPRESENTATION_SIZE = BITS_PER_INT;
		final int NUM_NODES = this.huffmanCodeTree.size();
		final int NODE_VALUES = (BITS_PER_WORD + 1) * (numberOfChunks() + 1); // Account for pseudo-eof
		if (headerFormat == STORE_COUNTS) {
			// SCF
			for (int k = 0; k < ALPH_SIZE; k++) {
				this.newSize += BITS_PER_INT;
			}
		} else if (headerFormat == STORE_TREE) {
			// STF
			this.newSize += TREE_REPRESENTATION_SIZE + NUM_NODES + NODE_VALUES;
		}
	}

	// Calculates the number of distinct "BITS_PER_WORD" chunks
	private int numberOfChunks() {
		int numOfChunks = 0;
		for (int i = 0; i < myCount.length; i++) {
			if (myCount[i] > 0) {
				numOfChunks++;
			}
		}
		return numOfChunks;
	}

	private void countCompressedDataSize(Map<Integer, String> encodedBits) {
		for (int chunk : encodedBits.keySet()) {
			if (chunk != PSEUDO_EOF) {
				int frequency = myCount[chunk];
				String encoding = encodedBits.get(chunk);
				this.newSize += encoding.length() * frequency;
			}
		}
	}

	public int getOldSize() {
		return this.originalSize;
	}

	public int getNewSize() {
		return this.newSize;
	}

}
