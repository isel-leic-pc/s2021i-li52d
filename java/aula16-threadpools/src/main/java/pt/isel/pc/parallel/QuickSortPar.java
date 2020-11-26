package pt.isel.pc.parallel;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import pt.isel.pc.utils.WorkerThreadReport;

import static pt.isel.pc.parallel.SortUtils.*;


/**
 * Show the (not good) behaviour of Java thread pools on
 * divide and conquer parallel algorithms.
 * The bad consequences are deadlock (FixedThreadPool)
 * or too many threads (CachedThreadPool)
 */
public class QuickSortPar {
    private static final int NTHREADS = Runtime.getRuntime().availableProcessors();

    private static final int THRESHOLD = 10000;
    private static final int FIXED_THREADPOOL_LEVEL=3;
    private static final int CACHED_THREADPOOL_LEVEL=20;

    private static AtomicInteger workUnits = new AtomicInteger();

    private final  ExecutorService pool;
    private final  Stats stats;
    private final int parLevel;

    public QuickSortPar(ExecutorService pool, int parLevel) {
        this.stats = new Stats();
        this.pool = pool;
        this.parLevel = parLevel;
    }

    private void sort(int[] vals, int low, int high, int level) {
        if (high - low < THRESHOLD) {
            seqSort(vals, low, high); return;
        }

        int pivot = part(vals, low, high);
        if (level > 0) {
            Future<?> f1 = pool.submit(() -> {
                stats.collect();;
                sort(vals, low, pivot,level-1);
            });
            Future<?> f2 = pool.submit(() -> {
                stats.collect();;
                sort(vals, pivot+1, high,level-1);
            });
            try {
                f1.get();
                f2.get();
            }
            catch(ExecutionException | InterruptedException e) { }
        }
        else {
            seqSort(vals, low, pivot);
            seqSort(vals, pivot+1, high);
        }
    }


    private void sort(int[] vals) {
        sort(vals, 0, vals.length-1, parLevel);
        stats.show();
        awaitShutdown(pool);
    }


    public static void main(String[] args) {
        int[] vals = new int[50000000];
        initVals(vals);

        /*
        testSort("Sequential sort", TestSortPools::seqSort, vals);

        QuickSortPar sorter = new QuickSortPar(
                Executors.newFixedThreadPool(NTHREADS), FIXED_THREADPOOL_LEVEL);
        testSort("FixedThreadPool sort", sorter::sort, vals );

        sorter = new QuickSortPar(
                Executors.newCachedThreadPool(), CACHED_THREADPOOL_LEVEL);
        testSort("CachedThreadPool sort", sorter::sort, vals );
        */

        QuickSortPar sorter = new QuickSortPar(
            Executors.newWorkStealingPool(), CACHED_THREADPOOL_LEVEL);
        testSort("WorkStealingPool sort", sorter::sort, vals );

        WorkerThreadReport.shutdownWorkerThreadReport();
    }


}
