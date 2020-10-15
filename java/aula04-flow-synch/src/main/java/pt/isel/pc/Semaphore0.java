package pt.isel.pc;

/**
 * A first simplified try to provide
 * a counter semaphore supported by an implicit monitor
 *
 */
public class Semaphore0 {
    private int permits;
    private Object monitor = new Object();

    public Semaphore0(int initialUnits) {
        permits = initialUnits;
    }

    public void acquire(int units)
            throws InterruptedException {
        synchronized (monitor) {
            while(units > permits)
               monitor.wait();
            permits -= units;
        }
    }

    public void release(int units) {
        synchronized (monitor) {
            permits += units;
            monitor.notifyAll();
        }
    }
}
