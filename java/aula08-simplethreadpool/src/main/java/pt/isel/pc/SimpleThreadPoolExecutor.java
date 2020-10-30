package pt.isel.pc;

import pt.isel.pc.utils.NodeList;
import pt.isel.pc.utils.TimeoutHolder;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleThreadPoolExecutor {

    private class PoolThread {
        public  Condition cond;
        public Runnable cmd;
        public Thread thread;

        public PoolThread( ) { }

        public PoolThread(Runnable cmd ) {
            nThreads++;
            this.cond = monitor.newCondition();
            this.cmd = cmd;
        }

        public void start() {
            thread = new Thread( () -> {
                execThread(this);
            });
            thread.start();
        }

        public void dispatchCmd(Runnable cmd) {
            this.cmd = cmd;
            cond.signal();
        }

        public void safeExec(Runnable cmd) {
            try {
               cmd.run();
            }
            catch(Exception e) {
                // do eventual log...
            }
        }

        public void safeExec() {
            safeExec(cmd);
        }

        public boolean hasCmd() {
            return cmd != null;
        }

        public void await(long timeout) throws InterruptedException {
            cmd = null;
            cond.await(timeout, TimeUnit.MILLISECONDS);
        }

        public void abort() {

        }
    }

    private class Request {
        public boolean accepted;
        public final Runnable cmd;
        public final Condition cond;

        public Request(Runnable cmd) {
            this.cond = monitor.newCondition();
            this.cmd = cmd;
        }

        public void accept() {
            this.accepted = true;
            this.cond.signal();
        }
    }

    private int maxPoolSize;    // max  worker threads
    private int keepAliveTime;  // max inactive time
    private int nThreads;       // current number of threads

    private NodeList<PoolThread> inactiveThreads;
    private NodeList<Request> pendingRequests;

    private Lock monitor;

    private Condition shutdownCompleted;

    // this method contains the life cycle of a worker thread
    private void execThread(PoolThread pthread) {
        // execute first request passed on thread creation
        pthread.safeExec();

        monitor.lock();
        do {
            Request req = null;
            Runnable cmd = null;
            try {
                if (!pendingRequests.empty()) {
                    req = pendingRequests.removeFirst();
                    req.accept();
                    cmd = req.cmd;
                }
                else {
                    TimeoutHolder th = new TimeoutHolder(keepAliveTime);
                    NodeList.Node<PoolThread> node =
                            inactiveThreads.addFirst(pthread);
                    do {
                        try {
                            pthread.await(th.remaining());
                            if (pthread.cmd != null) {
                                cmd = pthread.cmd;
                                break;
                            }
                            if (th.timeout()) {
                                inactiveThreads.remove(node);
                                pthread.abort();
                                return;
                            }
                        }
                        catch(InterruptedException e) {

                        }
                    }
                    while(true);

                }
            }
            finally {
                monitor.unlock();
            }

            // process new Runnable
            pthread.safeExec(cmd);
        }
        while(true);

    }

    public SimpleThreadPoolExecutor(int maxPoolSize, int keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
        this.maxPoolSize = maxPoolSize;
        this.monitor = new ReentrantLock();
        inactiveThreads = new NodeList<>();
        pendingRequests = new NodeList<>();
        shutdownCompleted = monitor.newCondition();
    }



    public boolean execute(Runnable command, int timeout)
            throws InterruptedException {
        monitor.lock();
        try {
            if (!inactiveThreads.empty()) {
                PoolThread pt = inactiveThreads.removeFirst();
                pt.dispatchCmd(command);
                return true;
            }
            if (nThreads < maxPoolSize) {
                PoolThread pt = new PoolThread(command);
                pt.start();
                return true;
            }

            // preparing wait
            TimeoutHolder th = new TimeoutHolder(timeout);
            Request req = new Request(command);
            NodeList.Node<Request> node = pendingRequests.addLast(req);
            do {
                try {
                    req.cond.await(th.remaining(), TimeUnit.MILLISECONDS);
                    if (req.accepted) return true;
                    if (th.timeout()) {
                        pendingRequests.remove(node);
                        return false;
                    }
                }
                catch( InterruptedException e) {
                    if (req.accepted) {
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    pendingRequests.remove(node);
                    throw e;
                }
            }
            while(true);
        }
        finally {
            monitor.unlock();
        }
    }

    public void shutdown() {
        monitor.lock();
        try {

        }
        finally {
            monitor.unlock();
        }

    }

    public boolean awaitTermination(int timeout) throws InterruptedException {
        monitor.lock();
        try {
            return false;
        }
        finally {
            monitor.unlock();
        }
    }

    public int numThreads() {
        monitor.lock();
        try {
            return nThreads;
        }
        finally {
            monitor.unlock();
        }
    }
}
