package pt.isel.pc.parallel;

import pt.isel.pc.utils.WorkerThreadReport;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import static pt.isel.pc.parallel.SortUtils.*;

/**
 * Just to illustrate ForkJoinPool usage
 * This is a different "beast" from other Java thread pools
 * that is used mainly to support Divide and Conquer parallel algorithms.
 * using the style below
 */
public class QuickSortParFJ {
    private final ForkJoinPool pool;
    private final Stats stats;

    private  class QuickSortTask extends RecursiveAction {
        private final int[] array;
        private final int low;
        private final int high;
        private static final int THRESHOLD = 10000;

        /**
         * Creates a {@code QuickSortTask} containing the array and the bounds of the array
         *
         * @param array the array to sort
         * @param low the lower element to start sorting at
         * @param high the non-inclusive high element to sort to
         */
        protected QuickSortTask(int[] array, int low, int high) {
            this.array = array;
            this.low = low;
            this.high = high;
        }

        @Override
        protected void compute() {
            stats.collect();
            if (high - low <= THRESHOLD) {
                seqSort(array, low, high-1);
            } else {
                int pivot = part(array,low,high-1);

                // Execute the sub tasks and wait for them to finish
                invokeAll(new QuickSortTask(array, low, pivot+1),
                         new QuickSortTask(array, pivot+1, high));
            }
        }
    }

    public QuickSortParFJ() {
        stats = new Stats();
        pool = new ForkJoinPool();
    }

    /**
     * Sorts all the elements of the given array using the ForkJoin framework
     * @param array the array to sort
     */
    public void sort(int[] array) {
        ForkJoinTask<Void> job = pool.submit(new QuickSortTask(array, 0, array.length));
        job.join();
        stats.show();
    }


    public static void main(String[] args) {
        // Prepare sort
        int[] vals = new int[50000000];
        initVals(vals);

        testSort("Sequential sort",  SortUtils::seqSort, vals);

        testSort("ForkJoin sort", new QuickSortParFJ()::sort, vals);

        WorkerThreadReport.shutdownWorkerThreadReport();
        System.out.println("done");
    }
}
