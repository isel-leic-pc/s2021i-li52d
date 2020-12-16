using SynchUtils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;

namespace Aula16_threadpools {
    class Stats {
        private volatile int workUnits;


        public Stats() {
            WorkerThreadReport.Clear();
            workUnits = 0;
        }

        public void Collect() {
            //int used = WorkerThreadReport.createdThreads();
            Interlocked.Increment(ref workUnits);

            //System.out.printf("used= %d, wi=%d\n", used, wi);
            WorkerThreadReport.RegisterWorker();
        }


        public void Show() {
            Console.WriteLine("Total of work units: {0}", workUnits);
            Console.WriteLine("Total of worker threads: {0}", WorkerThreadReport.CreatedThreads);
        }
    }
}
