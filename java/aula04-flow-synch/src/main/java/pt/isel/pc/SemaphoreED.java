package pt.isel.pc;

import pt.isel.pc.utils.TimeoutHolder;

public class SemaphoreED {
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
