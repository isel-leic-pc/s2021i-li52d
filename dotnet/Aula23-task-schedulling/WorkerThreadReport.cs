using System;
using System.Collections.Generic;
using System.Threading;

namespace Aula23_task_schedulling {

    public class WorkerThreadReport {
        // Static fields whose access is protected by the monitor lock.
        private static readonly object monitor = new object();
        private static int lastCreationTime = Environment.TickCount;
        private static int lastRefTime = Environment.TickCount;
        private static int createdThreads;
        private static readonly List<WorkerThreadReport>
           reports = new List<WorkerThreadReport>();

        // some options
        private static volatile bool verbose;

        // auxiliary functions
        private static void ShowMsg(String formatter, params object[] args) {
            if (verbose)
                Console.WriteLine(formatter, args);
        }

        private static void ShowMsg() {
            ShowMsg("");
        }

        public static void SetRefTime() {
            lock (monitor) {
                Thread.Sleep(50);
                lastRefTime = Environment.TickCount;
                Thread.Sleep(100);
            }
        }

        // Instance fields used by each worker thread.

        private readonly Thread theThread;
        private readonly int theThreadId;
        private int timeOfLastUse;
        private int exitTime;

        internal WorkerThreadReport() {
            theThread = Thread.CurrentThread;
            theThreadId = theThread.ManagedThreadId;
            int order, injectionDelay, now;
            lock (monitor) {
                timeOfLastUse = now = Environment.TickCount;
                injectionDelay = now - lastCreationTime;
                lastCreationTime = now;
                order = ++createdThreads;
                reports.Add(this);
            }
            ShowMsg("--> injected the {0}-th worker #{1}, after {2} ms",
                               order, theThreadId, injectionDelay);
        }

        // Thread local that holds the report for each worker thread.

        internal static ThreadLocal<WorkerThreadReport> report =
               new ThreadLocal<WorkerThreadReport>(() => new WorkerThreadReport());

        // Register or update a report for the current thread.
        public static void RegisterWorker() {
            report.Value.timeOfLastUse = Environment.TickCount;
        }

        // Returns the number of created threads
        public static int CreatedThreads {
            get { lock (monitor) return createdThreads; }
        }

        // Returns the number of used threads
        public static int UsedThreads {
            get {
                lock (monitor) {
                    int count = 0;
                    for (int i = 0; i < reports.Count; ++i) {
                        var r = reports[i];
                        if (r.timeOfLastUse > lastRefTime) count++;
                    }
                    return count;
                }
            }
        }

        // Returns the number of active threads
        public static int ActiveThreads {
            get { lock (monitor) return reports.Count; }
        }

        // Displays the alive worker threads
        public static void ShowThreads() {
            lock (monitor) {
                if (reports.Count == 0)
                    ShowMsg("-- no worker threads are alive");
                else
                    ShowMsg("-- {0} worker threads are alive:", reports.Count);
                foreach (WorkerThreadReport r in reports) {
                    ShowMsg(" #{0}", r.theThreadId);
                }
                ShowMsg();
            }
        }

        public static bool Verbose {
            get { return verbose; }
            set { verbose = value; }
        }

        // Thread that monitors the worker thread's exit.
        private static void ExitMonitorThreadBody() {
            int rcount;
            do {
                List<WorkerThreadReport> exited = null;
                lock (monitor) {
                    rcount = reports.Count;
                    for (int i = 0; i < reports.Count;) {
                        WorkerThreadReport r = reports[i];
                        if (!r.theThread.IsAlive) {
                            reports.RemoveAt(i);
                            if (exited == null) {
                                exited = new List<WorkerThreadReport>();
                            }
                            r.exitTime = Environment.TickCount;
                            exited.Add(r);
                        }
                        else
                            i++;
                    }
                }
                if (exited != null) {
                    foreach (WorkerThreadReport r in exited) {
                        ShowMsg("--worker #{0} exited after {1} s of inactivity",
                            r.theThreadId, (r.exitTime - r.timeOfLastUse) / 1000);
                    }
                }

                // sleep for a while.
                try {
                    Thread.Sleep(50);
                }
                catch (ThreadInterruptedException) {
                    return;
                }
            } while (true);
        }

        // The exit thread
        private static Thread exitThread;

        // Static constructor: start the exit monitor thread.
        static WorkerThreadReport() {
            exitThread = new Thread(ExitMonitorThreadBody);
            exitThread.Start();
        }

        // shutdown thread report
        public static void ShutdownWorkerThreadReport() {
            exitThread.Interrupt();
            exitThread.Join();
        }

        public static void Reset() {
            lock(monitor) {

                SetRefTime();
                
            }
           
        }


    }
}
