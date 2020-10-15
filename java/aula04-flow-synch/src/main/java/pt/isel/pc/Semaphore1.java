package pt.isel.pc;

import pt.isel.pc.utils.TimeoutHolder;

/**
 * A variant of the first Semaphore version that support
 * passing a timeout to the acquire operation
 */
public class Semaphore1 {
    private int permits;
    private Object monitor = new Object();

    public Semaphore1(int initialUnits) {
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
        // the syncronized block commented in the next line
        // is not really necessary since the overloaded acquire
        // called is already a synchronized methos but it doesn't
        // bring any problem if uncommented since the intrinsic monitors
        // lock are reentrant, i.e. they can acquired recursively
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
