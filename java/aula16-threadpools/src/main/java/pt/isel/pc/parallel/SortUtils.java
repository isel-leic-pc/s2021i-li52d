package pt.isel.pc.parallel;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SortUtils {


	public  static int part(int[] vals, int low, int high) {
		int med = (low + high) / 2;
		int pivot = vals[med];
		int i = low, j = high;
		while (i <= j) {

			while (vals[i] < pivot) ++i;
			while ( vals[j] > pivot) --j;
			if (i <= j) {
				int tmp = vals[i];
				vals[i] = vals[j];
				vals[j] = tmp;
				++i;
				--j;
			}
		}
		return i-1;
	}

	public static void seqSort(int[] vals, int low, int high) {
		if (low >= high) return;
		int pivot = part(vals, low, high);

		seqSort(vals, low, pivot);
		seqSort(vals, pivot + 1, high);

	}

	public static void seqSort(int[] vals ) {
		seqSort(vals, 0, vals.length-1);
	}


	public static void  initVals(int[] vals) {
		Random r = new Random();

		for(int i=0; i < vals.length; ++i) vals[i] = r.nextInt() % 100;
	}

	public static void  printVals(int[] vals) {
		for(int i=0; i < vals.length; ++i)
			System.out.println(vals[i]);
	}

	public static boolean isSorted(int[] vals) {
		for(int i=0; i < vals.length-1; ++i) {
			if (vals[i] > vals[i+1]) return false;
		}
		return true;
	}

	public static void testSort(String name, Consumer<int[]> sorter, int[] vals) {
		System.out.println();
		System.out.println(name);


		int[] valTest = vals.clone();

		long start = System.currentTimeMillis();

		sorter.accept(valTest);

		long end = System.currentTimeMillis();

		if (!isSorted(valTest))
			System.out.println("Sort error in "+ (end-start) + "ms!");
		else
			System.out.println(name + " done in " + (end-start) + "ms!");
	}

	public static void awaitShutdown(ExecutorService pool) {
		pool.shutdown();
		while(true) {
			try {
				pool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
				break;
			}
			catch(InterruptedException e) {
			}
		}
	}
}
