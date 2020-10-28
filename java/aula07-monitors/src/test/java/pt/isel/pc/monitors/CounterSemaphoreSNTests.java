package pt.isel.pc.monitors;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static pt.isel.pc.Utils.join;
import static pt.isel.pc.Utils.sleep;

public class CounterSemaphoreSNTests {

    private static final Logger log =
            LoggerFactory.getLogger(CounterSemaphoreSNTests.class);

    @Test
    public void simpleAcquiresTest()
            throws InterruptedException {
        CounterSemaphoreSN cs = new CounterSemaphoreSN(1);

        assertTrue(
                cs.acquire(1)
        );
        assertFalse(cs.acquire(1, 1000));
    }

    @Test
    public void simpleAcquireReleaseTest()
            throws InterruptedException {
        CounterSemaphoreSN cs = new CounterSemaphoreSN(0);

        int[] val = {0};

        Thread t = new Thread(() -> {
            try {
                cs.acquire(1);
                val[0] = 1;
            }
            catch(InterruptedException e) {
                val[0] = -1;
            }
        });
        t.start();

        cs.release(1);
        join(t, 3000);
        log.info("Value= {}", val[0]);
        assertEquals(1, val[0]);
    }

    @Test
    public void AcquireReleaseFifoOrderTest()
            throws InterruptedException {
        CounterSemaphoreSN cs = new CounterSemaphoreSN(0);
        final int nThreads = 10;
        int[] results = new int[nThreads];
        AtomicInteger idxRes = new AtomicInteger(0);
        CountDownLatch[] latches = new CountDownLatch[nThreads];
        CountDownLatch allDone = new CountDownLatch(nThreads);

        for (int i=0; i < nThreads; ++i)
            latches[i] = new CountDownLatch(1);

        for (int i=0; i < nThreads; ++i) {
            final int idx = i;
            Thread t = new Thread(() -> {
                try {
                    latches[idx].await();
                    cs.acquire(nThreads-idx);
                    results[idxRes.getAndIncrement()] = idx;
                    allDone.countDown();
                }
                catch(InterruptedException e) {
                    fail();
                }
            });
            t.start();
        }

        // unblock threads by order
        for (int i=0; i < nThreads; ++i) {
            latches[i].countDown();
            sleep(100);
        }

        int totalUnits = ((nThreads+1)*(nThreads))/2;

        // give one by one unit periodically
        for (int i=0; i < ((nThreads+1)*nThreads)/2; ++i) {
            cs.release(totalUnits);
            sleep(50);
        }

        allDone.await();

        for (int i=0; i < nThreads; ++i) {
            assertEquals(i, results[i]);
        }
    }
}
