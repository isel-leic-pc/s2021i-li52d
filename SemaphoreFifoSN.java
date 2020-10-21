package pt.isel.pc;

import pt.isel.pc.utils.NodeList;
import pt.isel.pc.utils.TimeoutHolder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SemaphoreFifoSN {
    private int permits;
    private Lock monitor = new ReentrantLock();

    private static class Request {
        public final int units;
        private boolean done;
        public final Condition condition;

        public Request(Condition condition,
                       int units) {
            this.condition = condition;
            this.units = units;
        }

        public void complete() {
            this.done = true;
            condition.signal();
        }

        public boolean isCompleted() {
            return done;
        }
    }

    private NodeList<Request> requests;

    private void notifyWaiters() {
        while ( !requests.empty() && permits >= requests.first().units) {
            Request req = requests.removeFirst();
            permits -= req.units;
            req.complete();
        }
    }

    public SemaphoreFifoSN(int initialUnits) {
        if (initialUnits > 0)
            permits = initialUnits;
        requests = new NodeList<>();
    }

    public boolean acquire(int units, long timeout)
            throws InterruptedException {
        monitor.lock();
        try {
            // non blocking path
            if (requests.empty() && permits >= units) {
                permits -= units;
                return true;
            }
            if (timeout == 0) {
                return false;
            }

            TimeoutHolder th = new TimeoutHolder(timeout);
            Request req = new Request(
                    monitor.newCondition(), units);
            NodeList.Node<Request> node = requests.addLast(req);
            do {
                try {
                    req.condition.await(
                            th.remaining(), TimeUnit.MILLISECONDS);
                    if (req.isCompleted()) {
                        return true;
                    }
                    if (th.timeout()) {
                        requests.remove(node);
                        notifyWaiters();
                        return false;
                    }
                } catch (InterruptedException e) {
                    if( req.isCompleted()) {
                        // delay the interruption and return success
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    requests.remove(node);
                    notifyWaiters();
                    throw e;
                }

            } while (true);
        }
        finally {
            monitor.unlock();
        }
    }

    public boolean acquire(int units)
            throws InterruptedException {
        return acquire(units, TimeoutHolder.INFINITE);
    }

    public void release(int units) {
        monitor.lock();
        try {
            permits += units;
            notifyWaiters();
        }
        finally {
            monitor.unlock();
        }
    }
}
