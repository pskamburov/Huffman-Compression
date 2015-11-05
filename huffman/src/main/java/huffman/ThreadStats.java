package huffman;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadStats {

	public static ConcurrentHashMap<String, AtomicLong> stats = new ConcurrentHashMap<String, AtomicLong>();

	public static void printStats() {
		List<AtomicLong> executions = new ArrayList<AtomicLong>(stats.values());
		long sum = 0;
		for (AtomicLong value : executions) {
			sum += value.get();
		}
		for (Entry<String, AtomicLong> entry : stats.entrySet()) {
			float x = ((float) entry.getValue().get() / (float) sum) * 100;
			System.out.println(entry.getKey() + " ---> " + x + " %");
		}
	}
}
