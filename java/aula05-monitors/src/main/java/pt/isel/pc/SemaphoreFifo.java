package pt.isel.pc;

import pt.isel.pc.utils.NodeList;
import pt.isel.pc.utils.TimeoutHolder;

/**
 * the counter semaphores (with multiple release and acquires)
 * aren't fair to the greater Acquisitions since
 *
 * In order to it fairly we have to maintain a list of pending
 * acquisitions and in this version we will do exactly that
 */
public class SemaphoreFifo {
    private int permits;
    private Object monitor = new Object();

    private static class Request {
        public final int units;
        public Request(int units) {
            this.units = units;
        }
    }

    private NodeList<Request> requests;

    private void notifyWaiters() {
       if ( !requests.empty() && permits >= requests.first().units)
           monitor.notifyAll();
    }

    public SemaphoreFifo(int initialUnits) {
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
                    if (requests.first() == req && permits >= req.units) {
                        requests.removeFirst();
                        permits -= req.units;
                        notifyWaiters();
                        return true;
                    }
                    if (th.timeout()) {
                        requests.remove(node);
                        notifyWaiters();
                        return false;
                    }
                } catch (InterruptedException e) {
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
