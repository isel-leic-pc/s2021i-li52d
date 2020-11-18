package pt.isel.pc;

import pt.isel.pc.utils.TimeoutHolder;

import java.util.concurrent.atomic.AtomicInteger;

public class CounterSemaphoreOpt {
    private final AtomicInteger  permits;
    private Object monitor = new Object();
    private volatile int waiters;

    public CounterSemaphoreOpt(int initialUnits) {
        permits = new AtomicInteger(initialUnits);
    }

    private boolean tryAcquire(int units) {
        /*
        do {
            int obs = permits.get();
            if (obs < units) return false;
            if (permits.compareAndSet(obs, obs - units))
                return true;
        }
        while(true);
         */
        int obs;
        do {
            obs = permits.get();
            if (obs < units) return false;
        }
        while(!permits.compareAndSet(obs, obs - units));
        return true;
    }

    public boolean acquire(int units, long timeout)
            throws InterruptedException {

        // fast path
        if (tryAcquire(units)) return true;
        synchronized (monitor) {
            if (timeout == 0)
                return false;
            // wait
            TimeoutHolder th = new TimeoutHolder(timeout);
            waiters++;
            if (tryAcquire(units))  {  waiters--; return true; }
            do {
                try {
                    monitor.wait(th.remaining());
                    if (tryAcquire(units)) {
                        return true;
                    }
                    if (th.timeout()) {
                        return false;
                    }
                }
                finally  {
                    waiters--;
                }
            } while(true);
        }
    }

    public boolean acquire(int units)
            throws InterruptedException {
        // the synchronized block commented in the next line
        // is not really necessary since the overloaded acquire
        // called is already a synchronized method but it doesn't
        // bring any problem if uncommented since the intrinsic monitors
        // lock are reentrant, i.e. they can be acquired recursively

        //synchronized(monitor) {
        return acquire(units, TimeoutHolder.INFINITE);
        //}
    }

    public void release(int units) {
        permits.addAndGet(units);
        if (waiters > 0) {
            synchronized (monitor) {
                if (waiters > 0 )
                     monitor.notifyAll();
            }
        }
    }
}
