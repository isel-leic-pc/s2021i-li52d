package pt.isel.pc;

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
    
    public SemaphoreFifo(int initialUnits) {
        permits = initialUnits;
    }

    public boolean acquire(int units, long timeout)
            throws InterruptedException {
        synchronized (monitor) {
            // non blocking path
            if (permits >= units) {
                permits -= units;
                return true;
            }
            if (timeout == 0)
                return false;
            // wait
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                monitor.wait(th.remaining());
                if (permits >= units ) {
                    permits -= units;
                    return true;
                }
                if (th.timeout()) return false;
            } while(true);
        }
    }

    public boolean acquire(int units)
            throws InterruptedException {
        //synchronized(monitor) {
        return acquire(units, TimeoutHolder.INFINITE);
        //}
    }

    public void release(int units) {
        synchronized (monitor) {
            permits += units;
            monitor.notifyAll();
        }
    }
}
