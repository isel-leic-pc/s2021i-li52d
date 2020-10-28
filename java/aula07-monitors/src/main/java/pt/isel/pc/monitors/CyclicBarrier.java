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
    private int remaining;

    private Lock monitor;
    private Condition passBarrier;

    private enum State { Opened, Broken, Closed };

    private BatchRequestQueue<State> queue;
    private boolean broken;

    public CyclicBarrier(int partners) {
        this.monitor = new ReentrantLock();
        this.passBarrier = monitor.newCondition();
        this.partners = partners;
        this.remaining = partners;
        this.queue = new BatchRequestQueue<>(State.Closed);
    }

    private void nextSynch() {
        remaining = partners;
        queue.newBatch(State.Closed);
    }

    private void openBarrier() {
        if (queue.size() > 0) {
            BatchRequestQueue.Request<State> req =
                        queue.current();
            req.value = State.Opened;
            passBarrier.signalAll();
        }

        nextSynch();
    }

    private void breakBarrier() {
        if (queue.size() > 0) {
            BatchRequestQueue.Request<State> req =
                    queue.current();
            req.value = State.Broken;
            passBarrier.signalAll();
        }
        broken = true;
    }


    public int await(long timeout)
            throws  InterruptedException,
                    BrokenBarrierException,
                    TimeoutException {
        monitor.lock();
        try {
            if (broken) {
                throw new BrokenBarrierException();
            }
            int index = --remaining;
            if (index == 0) {
                openBarrier();
                return 0;
            }
            TimeoutHolder th = new TimeoutHolder(timeout);
            BatchRequestQueue.Request<State> req = queue.add();
            do {
                try {
                    passBarrier.await(th.remaining(), TimeUnit.MILLISECONDS);
                    if (req.value == State.Opened) return index;
                    if (req.value == State.Broken)
                        throw new BrokenBarrierException();
                    if (th.timeout()) {
                        queue.remove(req);
                        breakBarrier();
                        throw new TimeoutException();
                    }
                }
                catch(InterruptedException e) {
                    if (req.value == State.Opened) {
                        Thread.currentThread().interrupt();
                        return index;
                    }
                    breakBarrier();
                    queue.remove(req);
                    throw e;
                }
            }
            while(true);
        }
        finally {
            monitor.unlock();
        }
    }

    public void reset() {
        monitor.lock();
        try {
            breakBarrier();
            nextSynch();
        }
        finally {
            monitor.unlock();
        }
    }

    public boolean isBroken() {

        monitor.lock();
        try {
             return broken;
        }
        finally {
            monitor.unlock();
        }

    }
}
