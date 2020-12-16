
using System;

using System.Diagnostics;

using System.Threading;
using System.Threading.Tasks;

namespace Aula23_task_schedulling {

    public class ParQuickSort {
        private const int THRESHOLD = 1000;

        private int level;
        Stats stats;

        public ParQuickSort(int level) {
            this.level = level;
            stats = new Stats();
        }

        // utils
        private static void InitVals(int[] vals) {
            Random r = new Random();

            for (int i = 0; i < vals.Length; ++i) vals[i] = r.Next() % 100;
        }

        private static void PrintVals(int[] vals, int low, int high) {
            Random r = new Random();

            for (int i = low; i <= high; ++i) Console.WriteLine(vals[i]);
        }

        private static bool IsSorted(int[] vals) {
            for (int i = 0; i < vals.Length - 1; ++i) {
                if (vals[i] > vals[i + 1]) return false;
            }
            return true;
        }

        // sort versions
        private static int Part(int[] vals, int low, int high) {
            int med = (low + high) / 2;
            int pivot = vals[med];
            int i = low, j = high;
            while (i <= j) {

                while (i <= high && vals[i] < pivot) ++i;
                while (j >= low && vals[j] > pivot) --j;
                if (i <= j) {
                    int tmp = vals[i];
                    vals[i] = vals[j];
                    vals[j] = tmp;
                    ++i;
                    --j;
                }

            }

            return i - 1;
        }

        public static void SeqSort(int[] vals, int low, int high) {
            if (low >= high) return;
            int pivot = Part(vals, low, high);
          
            SeqSort(vals, low, pivot);
            SeqSort(vals, pivot + 1, high);
        }

        public  void SortTP(int[] vals, int low, int high, int level)  {
         
            if (high - low < THRESHOLD) { SeqSort(vals, low, high); return; }
            int pivot = Part(vals, low, high);

            if (level > 0) {
                CountdownEvent latch = new CountdownEvent(2);
                ThreadPool.QueueUserWorkItem((s) => {
                    stats.Collect();
                    SortTP(vals, low, pivot, level -1);
                    latch.Signal();
                }, null);
                ThreadPool.QueueUserWorkItem((s) => {
                    stats.Collect();
                    SortTP(vals, pivot + 1, high,level-1);
                    latch.Signal();
                }, null);
                latch.Wait();
            }
            else {
                SeqSort(vals, low, pivot);
                SeqSort(vals, pivot + 1, high);
            }

          

        }

        public void SortTasks(int[] vals, int low, int high, int level) {

            if (high - low < THRESHOLD) { SeqSort(vals, low, high); return; }
            int pivot = Part(vals, low, high);

            if (level > 0) {
               
                Task t1 = Task.Run(() => {
                    stats.Collect();
                    SortTasks(vals, low, pivot, level - 1);
                });
                Task t2 = Task.Run(() => {
                    stats.Collect();
                    SortTasks(vals, pivot + 1, high, level - 1);    
                });
                Task.WaitAll(t1, t2);
            }
            else {
                SeqSort(vals, low, pivot);
                SeqSort(vals, pivot + 1, high);
            }

        }


        public void SortTP(int[] vals) {
            stats.Clear();
            SortTP(vals, 0, vals.Length-1,level);
            stats.Show();
        }

        public void SortTasks(int[] vals) {
         
            SortTasks(vals, 0, vals.Length - 1, level);
            stats.Show();
            stats.Clear();
        }

        public static void SortSeq(int[] vals) {
            SeqSort(vals, 0, vals.Length - 1);
        }

        public delegate void Sorter(int[] vals);
       

        private const int LEVEL = 12;
        
        public static void Test(Sorter sort, int[] vals, String msg) {
            int[] auxVals = (int[])vals.Clone();
            Console.WriteLine("Start {0}...", msg);
            Stopwatch sw = Stopwatch.StartNew();

            sort(auxVals);
            sw.Stop();
            if (!IsSorted(auxVals)) Console.WriteLine("Sort Error!");
            else Console.WriteLine("{0} in {1}ms", msg, sw.ElapsedMilliseconds);
            Console.WriteLine();
        }

        public static void Test() {
         
            int[] vals = new int[50000000];
            InitVals(vals);
           
            Test(SortSeq, vals, "Sequential sort");

            ParQuickSort sort = new ParQuickSort(LEVEL);
        

            // Test(sort.SortTP, vals, "Threadpool sort");

            Test(sort.SortTasks, vals, "Tasks sort");


            WorkerThreadReport.ShutdownWorkerThreadReport();
        }
    }
}
