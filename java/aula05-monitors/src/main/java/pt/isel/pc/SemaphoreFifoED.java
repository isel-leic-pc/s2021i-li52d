package pt.isel.pc;

import pt.isel.pc.utils.NodeList;
import pt.isel.pc.utils.TimeoutHolder;

public class SemaphoreFifoED {
    private int permits;
    private Object monitor = new Object();

    private static class Request {
        public final int units;
        private boolean done;

        public Request(int units) {
            this.units = units;
        }
        public void complete() {
            this.done = true;
        }
        public boolean isCompleted() {
            return done;
        }
    }

    private NodeList<Request> requests;

    private void notifyWaiters() {
        boolean toNotify = false;
        while ( !requests.empty() && permits >= requests.first().units) {
            Request req = requests.removeFirst();
            permits -= req.units;
            req.complete();
            toNotify = true;
        }
        if (toNotify)
            monitor.notifyAll();
    }

    public SemaphoreFifoED(int initialUnits) {
        if (initialUnits > 0)
            permits = initialUnits;
        requests = new NodeList<>();
    }

    public boolean acquire(int units, long timeout)
            throws InterruptedException {
        synchronized (monitor) {
            // non blocking path
            if (requests.empty() && permits >= units) {
                permits -= units;
                return true;
            }
            if (timeout == 0) {
                return false;
            }

            TimeoutHolder th = new TimeoutHolder(timeout);
            Request req = new Request(units);
            NodeList.Node<Request> node = requests.addLast(req);
            do {
                try {
                    monitor.wait(th.remaining());
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
    }

    public boolean acquire(int units)
            throws InterruptedException {
        return acquire(units, TimeoutHolder.INFINITE);
    }

    public void release(int units) {
        synchronized(monitor) {
            permits += units;
            notifyWaiters();
        }
    }
}
