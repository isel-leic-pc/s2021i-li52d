
using System;
using System.Threading;


namespace Aula23_task_schedulling {
    class Stats {
        private volatile int workUnits;


        public Stats() {
            WorkerThreadReport.Reset();
            workUnits = 0;
        }

        public void Collect() {
            //int used = WorkerThreadReport.createdThreads();
            Interlocked.Increment(ref workUnits);

            //System.out.printf("used= %d, wi=%d\n", used, wi);
            WorkerThreadReport.RegisterWorker();
        }


        public void Clear() {
            WorkerThreadReport.Reset();
            workUnits = 0;
        }

        public void Show() {
            Console.WriteLine("Total of work units: {0}", workUnits);
            //Console.WriteLine("Total of worker threads: {0}", WorkerThreadReport.UsedThreads);
        }
    }
}
