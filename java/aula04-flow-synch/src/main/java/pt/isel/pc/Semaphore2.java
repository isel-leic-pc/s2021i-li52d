package pt.isel.pc;

import pt.isel.pc.utils.TimeoutHolder;

/**
 * This is a restricted version of Semaphore that
 * supports only single release and acquire operations.
 * Note that in this case we use notify method instead, since
 * there is no gain in awake more than a thread at a time.
 * But this comes with a responsibility.
 * We need to catch the InterruptedException in order to regenerate
 * the notification in case it occurs simultaneously with a
 * thread interruption to avoid lost notifications
 */
public class Semaphore2 {
    private int permits;
    private Object monitor = new Object();

    public Semaphore2(int initialUnits) {
        permits = initialUnits;
    }

    public boolean acquire(long timeout)
            throws InterruptedException {
        synchronized (monitor) {
            // non blocking path
            if (permits >= 1) {
                permits -= 1;
                return true;
            }
            if (timeout == 0)
                return false;
            // wait
            TimeoutHolder th = new TimeoutHolder(timeout);
            try {
                do {
                    monitor.wait(th.remaining());
                    if (permits >= 1) {
                        permits -= 1;
                        return true;
                    }
                    if (th.timeout()) return false;
                } while (true);
            }
            catch(InterruptedException e) {
                if (permits > 0)
                    monitor.notify();
                throw e;
            }
        }
    }

    public boolean acquire()
            throws InterruptedException {
        //synchronized(monitor) {
        return acquire(TimeoutHolder.INFINITE);
        //}
    }

    public void release() {
        synchronized (monitor) {
            permits += 1;
            monitor.notify();
        }
    }
}
