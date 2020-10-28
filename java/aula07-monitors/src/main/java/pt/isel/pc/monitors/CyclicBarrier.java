package pt.isel.pc.monitors;

import pt.isel.pc.utils.BatchRequestQueue;
import pt.isel.pc.utils.TimeoutHolder;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicBarrier {
    private int partners;
    private Lock monitor;
    private int remaining;
    private Condition passBarrier;

    private enum State { Opened, Broken, Closed };
    private State state;

    public CyclicBarrier(int partners) {

    }

    private void nextSynch() {

    }
    private void openBarrier() {

    }

    private void breakBarrier() {

    }


    public int await(long timeout)
            throws  InterruptedException,
                    BrokenBarrierException,
                    TimeoutException {
        monitor.lock();
        try {
            return 0;
        }
        finally {
            monitor.unlock();
        }
    }

    public void reset() {
        monitor.lock();
        try {

        }
        finally {
            monitor.unlock();
        }
    }

    public boolean isBroken() {
        monitor.lock();
        try {
            return false;
        }
        finally {
            monitor.unlock();
        }
    }
}
