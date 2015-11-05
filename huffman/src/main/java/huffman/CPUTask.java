package huffman;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

class CPUTask implements Runnable {

	private final BlockingQueue<byte[]> queue;
	private ConcurrentHashMap<Integer, AtomicLong> frequencyTable;

	public CPUTask(BlockingQueue<byte[]> queue,
			ConcurrentHashMap<Integer, AtomicLong> frequencyTable) {
		this.queue = queue;
		this.frequencyTable = frequencyTable;
	}

	@Override
	public void run() {
		byte[] readBytes = null;
		while (true) {
			try {
				// block if the queue is empty
				readBytes = queue.take();
				// threads statistics
//				AtomicLong temporary = new AtomicLong(1);
//				AtomicLong previous = ThreadStats.stats.putIfAbsent(Thread
//						.currentThread().getName(), temporary);
//				if (previous != null) {
//					previous.incrementAndGet();
//				}
				
				for (int i = 0; i < readBytes.length; i++) {
					AtomicLong temp = new AtomicLong(1);
					AtomicLong prev = frequencyTable.putIfAbsent(
							Byte.toUnsignedInt(readBytes[i]), temp);
					if (prev != null) {
						prev.incrementAndGet();
					}
				}

			} catch (InterruptedException e) {
				break; // FileTask has completed
			}
		}
	}
}