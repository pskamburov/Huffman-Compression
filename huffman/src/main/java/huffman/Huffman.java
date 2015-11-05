package huffman;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Huffman {

	private HuffmanTree huffmanTree = new HuffmanTree();

	private final static int QUEUE_SIZE = 200;

	public static void main(String[] args) {

		String FILENAME = "testme.txt";
		int THREADS = 6;
		Huffman huffman = new Huffman();
		if (args.length == 4
				&& (args[0].equalsIgnoreCase("-f") || args[0]
						.equalsIgnoreCase("-file"))
				&& (args[2].equalsIgnoreCase("-t") || args[2]
						.equalsIgnoreCase("-tasks"))) {
			//command line parameters
			FILENAME = args[1];
			THREADS = Integer.parseInt(args[3]);
			
			long startTime = System.currentTimeMillis();
			if (THREADS == 1) {
				//single thread is handled differently
				HashMap<Integer, Integer> table = huffman.oneThread(FILENAME);
				long endTime = System.currentTimeMillis();
				printCompressionTime(startTime, endTime);
				System.out.println(table);
			} else {
				//multiple threads
				ConcurrentHashMap<Integer, AtomicLong> frequencyTable = huffman
						.frequenceTable(THREADS, FILENAME);
				long endTime = System.currentTimeMillis();
				printCompressionTime(startTime, endTime);
				System.out.println(frequencyTable);

			}
			System.out.println("Completed");
			
		} else if (args.length == 1 && args[0].equals("-g")) {
			//start GUI if parameter is '-g'
			System.out.println("Graphic UI");
			GraphicUI gui = new GraphicUI();
			gui.BuildGUI();
		} else {
			System.err
					.println("Use format: [-f|-file]<filename> [-t|-tasks] <number>");
			System.err
			.println("Or just use the GUI: -g");
		}

	}

	private static void printCompressionTime(long startTime, long endTime) {
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.println("Execution time is "
				+ formatter.format((endTime - startTime) / 1000d)
				+ " seconds");
	}

	public ConcurrentHashMap<Integer, AtomicLong> frequenceTable(int threads,
			String filename) {

		FileTask.FILENAME = filename;
		ConcurrentHashMap<Integer, AtomicLong> frequencyTable = new ConcurrentHashMap<>();
		BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(QUEUE_SIZE);

		ExecutorService service = Executors.newFixedThreadPool(threads);

		for (int i = 0; i < (threads - 1); i++) {
			service.submit(new CPUTask(queue, frequencyTable));
		}
		int endingZerosInFile = 0;
		try {
			// number of zero's that should be removed
			endingZerosInFile = service.submit(new FileTask(queue)).get();
		} catch (InterruptedException | ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while (queue.size() > 0) {
			try {
				// Waiting the queue to be processed
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		service.shutdownNow(); // interrupt CPUTasks
		try {
			while (!service.awaitTermination(10, TimeUnit.SECONDS)) {
				System.out.println("Awaiting completion of threads.");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		removeZerosAtEnd(frequencyTable, endingZerosInFile);
		return frequencyTable;

	}

	public HashMap<Integer, Integer> oneThread(String filename) {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		try (InputStream br = new BufferedInputStream(new FileInputStream(
				filename))) {
			int symbol;// ASCII code
			while ((symbol = br.read()) != -1) {
				if (map.putIfAbsent(symbol, 1) != null) {
					Integer frequence = map.get(symbol);
					map.put(symbol, new Integer(frequence + 1));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	/**
	 * Compress a file using Huffman's encryption
	 * 
	 * @param originalFile
	 * @param compressedFile
	 */
	public void compressFile(String originalFile, String compressedFile) {
		HashMap<Integer, CodeSymbol> codeTable = huffmanTree.codeTable();
		System.out.println("Code Table: " + codeTable);
		compressFile(codeTable, originalFile, compressedFile);

	}

	/**
	 * Compress file (algorithm)
	 * 
	 * @param codesTable
	 *            - Hash<ASCI CODE, CodeSymbol(Bits..)>
	 * @param originalFile
	 * @param compressedFile
	 */
	private void compressFile(HashMap<Integer, CodeSymbol> codesTable,
			String originalFile, String compressedFile) {
		int nRead = 0;
		long currentByte = 0;
		int currentSize = 64;
		File x = new File(originalFile);
		long sizeOfAllBits = x.length();

		try (InputStream fis = new FileInputStream(originalFile);
				OutputStream fos = new FileOutputStream(compressedFile);
				DataOutputStream os = new DataOutputStream(fos);) {
			os.writeLong(sizeOfAllBits);
			while ((nRead = fis.read()) != -1) {
				CodeSymbol codeSymbol = codesTable.get(nRead);
				sizeOfAllBits++;
				long byteToFill = codeSymbol.code;
				int requiredSize = currentSize - codeSymbol.length;
				if (requiredSize == 0) {
					// shift length and OR
					currentByte = (long) (byteToFill | currentByte);
					os.writeLong((currentByte));
					currentByte = 0;
					currentSize = 64;
				} else if (requiredSize > 0) {
					currentSize = requiredSize;
					currentByte = (long) ((byteToFill << currentSize) | currentByte);
				} else {
					currentByte = (long) (currentByte | (byteToFill >> (0 - requiredSize)));
					os.writeLong(currentByte);
					currentSize = 64;
					currentByte = 0;
					// requiredSize is negative
					currentSize = (currentSize + requiredSize);
					currentByte = (long) (byteToFill << currentSize);
				}
			}
			os.writeLong(currentByte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Decrypt Huffman encrypted file.
	 * 
	 * @param filename
	 */
	public void decompressFile(String filename, String destination) {
		try (InputStream fis = new FileInputStream(filename);
				DataInputStream dis = new DataInputStream(fis);
				OutputStream fos = new FileOutputStream(destination);) {
			int nRead = 0;
			long size = dis.readLong();
			long currentSize = 0;
			Node currentNode = huffmanTree.getRoot();
			x: while ((nRead = dis.read()) != -1) {
				for (int i = 7; i >= 0; i--) {
					if (currentSize == size)
						break x;
					if (currentNode.getLeft() == null
							&& currentNode.getRight() == null) {
						currentSize++;
						fos.write(currentNode.getSymbol());
						i++;
						currentNode = huffmanTree.getRoot();
					} else {
						if (IsBitSet((byte) nRead, i)) {
							currentNode = currentNode.getRight();
						} else {
							currentNode = currentNode.getLeft();
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setHuffmanTree(HuffmanTree huffmanTree) {
		this.huffmanTree = huffmanTree;
	}

	public void importHuffmanTree(String filename) {
		huffmanTree.restoreTree(filename);
	}

	public void exportHuffmanTree(String filename) {
		huffmanTree.exportTree(filename);
	}

	public static String printBinary(long x) {
		StringBuilder st = new StringBuilder();
		for (int i = 0; i < Long.numberOfLeadingZeros((long) x); i++) {
			st.append("0");
		}
		st.append(Long.toBinaryString((long) x));
		return st.toString();

	}

	private static void removeZerosAtEnd(
			ConcurrentHashMap<Integer, AtomicLong> frequencyTable, int x) {
		AtomicLong countOfZeros = frequencyTable.get(0);
		// System.out.println(countOfZeros);
		if (countOfZeros != null) {
			if (countOfZeros.get() > x) {
				System.out.println(frequencyTable.replace(0, countOfZeros,
						new AtomicLong(countOfZeros.get() - x)));
			} else {
				frequencyTable.remove(0);
			}
		}
	}

	private boolean IsBitSet(byte b, int index) {
		int mask = 1 << index;
		return (b & mask) != 0;
	}

	/**
	 * Prints file bit by bit. Used for debugging purposes during developing the project.
	 * 
	 * @param filename
	 */
	private void readFile(String filename) {
		System.out.println("reading file");
		try (InputStream fis = new FileInputStream(filename);) {
			int nRead = 0;
			while ((nRead = fis.read()) != -1) {
				// System.out.println("symbol:" + nRead);
				for (int i = 7; i >= 0; i--) {
					System.out.print(IsBitSet((byte) nRead, i) ? 1 : 0);
				}
				// System.out.println();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
