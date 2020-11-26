package pt.isel.pc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerThreadReport {

    // Static fields whose access is protected by lock.
    private static final Lock monitor = new ReentrantLock();
    private static long lastCreationTime = System.currentTimeMillis();
    private static int createdThreads;
    private static final List<WorkerThreadReport> reports = new ArrayList<WorkerThreadReport>();
    private static boolean verbose;

    private static volatile boolean shutingDown = false;

    // options
    public static void setVerbose(boolean verbose) {
        WorkerThreadReport.verbose = verbose;
    }

    private static void showMsg(String formatter, Object ... args) {
        if (verbose)
            System.out.printf(formatter, args);
    }

    private static void showMsg() {
        showMsg("");
    }

    public static  void clear() {
        monitor.lock();
        try {
            createdThreads = 0;
            reports.clear();
        }
        finally {
            monitor.unlock();
        }
    }
    //
    // Instance fields used by each worker thread.
    //

    private final Thread theThread;
    private final long theThreadId;
    private long timeOfLastUse;
    private long exitTime;

    private WorkerThreadReport() {
        // get the new thread identity
        theThread = Thread.currentThread();
        theThreadId = theThread.getId();
        monitor.lock();
        long now, injectionDelay; int order;
        try {
            timeOfLastUse = now = System.currentTimeMillis();
            injectionDelay = now - lastCreationTime;
            lastCreationTime = now;
            order = ++createdThreads;
            reports.add(this);
        } finally {
            monitor.unlock();
        }
        showMsg("-> injected %d-th worker #%d, after %d ms%n",
                order, theThreadId, injectionDelay);
    }

    // Thread local that holds the report object for each worker thread.
    private static ThreadLocal<WorkerThreadReport> report =
            ThreadLocal.withInitial(() -> new WorkerThreadReport());


    // Register or update a report for the current thread.
    public static void registerWorker() {
        report.get().timeOfLastUse = System.currentTimeMillis();
    }

    // Returns the number of created threads
    public static int createdThreads() {
        monitor.lock();
        try {
            return createdThreads;
        } finally {
            monitor.unlock();
        }
    }

    // Returns the currently active threads
    public static int activeThreads() {
        monitor.lock();
        try {
            return reports.size();
        } finally {
            monitor.unlock();
        }
    }

    public static void showThreads() {
        monitor.lock();
        try {
            if (reports.size() == 0)
                showMsg("-- no worker threads alive");
            else {
                showMsg("-- %d worker threads are still alive:", reports.size());
                for (WorkerThreadReport r : reports) {
                    showMsg(" #%02d", r.theThreadId);
                }
                showMsg();
            }
        } finally {
            monitor.unlock();
        }
    }

    // The thread that monitors the worker thread's exit.
    private static final Runnable exitMonitorThreadBody = new Runnable() {
        public void run() {
            int rsize;
            do {
                List<WorkerThreadReport> exited = null;
                monitor.lock();
                rsize = reports.size();
                try {
                    for (int i = 0; i < reports.size(); ) {
                        WorkerThreadReport r = reports.get(i);
                        if (!r.theThread.isAlive()) {
                            reports.remove(i);
                            if (exited == null) {
                                exited = new ArrayList<WorkerThreadReport>();
                            }
                            r.exitTime = System.currentTimeMillis();									exited.add(r);
                        } else {
                            i++;
                        }
                    }
                } finally {
                    monitor.unlock();
                }
                if (exited != null) {
                    for(WorkerThreadReport r : exited) {
                        showMsg("--worker #%02d exited after shutdown or %d s of inactivity%n",
                                r.theThreadId, (r.exitTime - r.timeOfLastUse) / 1000);
                    }
                }

                // Sleep for a while.
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {}
            } while (!(shutingDown && rsize == 0));
        }
    };

    // Static constructor: start the exit monitor thread.

    private static final Thread exitThread;
    static {
        exitThread = new Thread(exitMonitorThreadBody);
        //exitThread.setDaemon(true);
        exitThread.start();
    }

    // shutdown thread report
    public static void shutdownWorkerThreadReport() {
        shutingDown = true;
        try {
            exitThread.join();
        } catch (InterruptedException ie) {}
    }
}