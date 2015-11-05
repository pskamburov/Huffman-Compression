package huffman;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

class FileTask implements Callable<Integer> {

	public static String FILENAME;

	private static final int SIZE = 2048;
	private final BlockingQueue<byte[]> queue;

	public FileTask(BlockingQueue<byte[]> queue) {
		this.queue = queue;
	}

	/**
	 * Return count of zero's at the end of the file
	 */
	@Override
	public Integer call() {

		InputStream br = null;
		Integer maxCounter = null;
		try {
			br = new BufferedInputStream(new FileInputStream(FILENAME), SIZE);
			byte[] bytesReadAtOnce = new byte[SIZE];
			maxCounter = SIZE - (br.available() % SIZE);
			while (br.read(bytesReadAtOnce) != -1) {
				// block if the queue is full
				queue.put(bytesReadAtOnce);
				bytesReadAtOnce = new byte[SIZE];
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("queue is full!");
			// e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return maxCounter;
	}
}