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
        Thread t;

        public PoolThread( ) { }

        public PoolThread(Runnable cmd ) {
            totalThreads++;
            this.cond = monitor.newCondition();
            this.cmd = cmd;
        }

        public void start() {
            Thread t = new Thread(() -> {
                execThread(this);
            });
            t.start();
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
                // log?
            }
        }

        public void safeExec() {
            safeExec(cmd);
            cmd = null;
        }

        public boolean hasCmd() { return cmd != null; }

        public void await(long timeout) throws InterruptedException {
            cmd = null;
            cond.await(timeout, TimeUnit.MILLISECONDS);
        }

        public void abort() {
            totalThreads--;
            if (totalThreads == 0 && inShutdown) {
                destroyed = true;
                shutdownCompleted.signalAll();
            }
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

    private NodeList<PoolThread> inactiveThreads;
    private NodeList<Request> pendingRequests;
    private int maxPoolSize;    // max  worker threads
    private int keepAliveTime;  // max inactive time
    private Lock monitor;
    private int totalThreads;
    private Condition shutdownCompleted;
    private boolean inShutdown, destroyed;


    private void threadQuit(PoolThread  pthread,  NodeList.Node<PoolThread> node) {
        if (node != null) inactiveThreads.remove(node);
        pthread.abort();
    }

    private void execThread(PoolThread pthread) {
        // immediately execute if command received
        if (pthread.hasCmd()) pthread.safeExec();
        do {
            Request req = null;
            Runnable cmd = null;
            monitor.lock();
            try {
                if (!pendingRequests.empty()) {
                    req = pendingRequests.removeFirst();
                    req.accept();
                }
                else if (inShutdown) {
                    threadQuit(pthread, null);
                    return;
                }
                if (req != null) cmd = req.cmd;
                else {
                    // prepare wait
                    TimeoutHolder th = new TimeoutHolder(keepAliveTime);
                    NodeList.Node<PoolThread> node = inactiveThreads.addFirst(pthread);
                    do {
                        try {
                            pthread.await(th.remaining());
                            if (pthread.hasCmd()) {
                                cmd = pthread.cmd;
                                break;
                            }
                            if (th.timeout() ||  inShutdown ) {
                                threadQuit(pthread, node);
                                return;
                            }
                        }
                        catch(InterruptedException e) {
                            threadQuit(pthread, node);
                            return;
                        }
                    }
                    while(true);
                }
            }
            finally {
                monitor.unlock();
            }
            if (cmd != null)
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
            if (inShutdown || destroyed)
                throw new RejectedExecutionException();
            if (!inactiveThreads.empty()) {
                // send the command to the inactive thread and wakeup it
                // explicitly
                PoolThread pt = inactiveThreads.removeFirst();
                pt.dispatchCmd(command);
                return true;
            }
           if (totalThreads < maxPoolSize) {
                PoolThread nt = new PoolThread(command);
                nt.start();
                return true;
            }

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
                } catch (InterruptedException e) {
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
            inShutdown = true;
            if (totalThreads == 0) {
                destroyed = true;
                shutdownCompleted.signalAll();
                return;
            }
            for( PoolThread pt : inactiveThreads) {
                pt.cond.signal();
            }
        }
        finally {
            monitor.unlock();
        }

    }

    public boolean awaitTermination(int timeout) throws InterruptedException {
        monitor.lock();
        try {
            if (destroyed) return true;
            if (timeout == 0) return false;
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                shutdownCompleted.await(th.remaining(), TimeUnit.MILLISECONDS);
                if (destroyed) return true;
                if (th.timeout()) return false;
            }
            while(true);
        }
        finally {
            monitor.unlock();
        }
    }

    public int numThreads() {
        monitor.lock();
        try {
            return totalThreads;
        }
        finally {
            monitor.unlock();
        }
    }
}
