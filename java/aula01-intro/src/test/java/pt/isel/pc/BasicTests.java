package pt.isel.pc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static pt.isel.pc.utils.ThreadUtils.sleep;

public class BasicTests {

    private static final int NTHREADS = 20;
    private static final int NREPS = 100000;

    private static final Logger log =
            LoggerFactory.getLogger(BasicTests.class);

    private static void helloThreadFunc() {
        Thread t =  Thread.currentThread();

        sleep(3000);

        log.info("Hello from thread {}", t.getId());
        log.info("new thread state is {} ", t.getState());
    }

    @Test
    public void firstThreadInJavaTest()
            throws InterruptedException {
        Thread thread = new Thread(BasicTests::helloThreadFunc);
        log.info("test thread is {} ", Thread.currentThread().getId());
        log.info("after creation, new thread in state {} ",  thread.getState());

        // System.out.println("new thread in state " + thread.getState());

        thread.start();

        // what if the next line is removed?
        thread.join();
    }

    @Test
    public void firstThreadInJavaWithLambdaTest()
            throws InterruptedException {

        Thread thread = new Thread(() -> {
            helloThreadFunc();
        });
        log.info("test thread is {} ", Thread.currentThread().getId());
        System.out.println("new thread in state " + thread.getState());

        thread.start();
        thread.join();
        log.info("thread 'thread' is alive? {0}", thread.isAlive());
        System.out.println("new thread in state " + thread.getState());
    }

    private int counter;

    @Test
    public void incrementCounterMultiThreadedWithoutSynchTest()
                                throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i=0; i < NTHREADS; ++i) {
            Thread t = new Thread( () -> {
                for(int j= 0; j < NREPS; ++j) {
                    counter++;

                    /*
                        inc(counter);

                        even if a single machine code is emmitted,
                        the execution is not atomic from the point of view
                        of memory access
                    */
                    /*
                       mov counter, r1
                       r1 = r1 +1
                       mov r1, counter

                       In the more common scenario of three machine code
                       emission, the hazzard pattern in the code is classic:
                       observe the value and act based on the observed value
                       in a non-atomic way
                     */
                }
            });
            t.start();
            threads.add(t);
        }

        // threads.forEach(t -> join(t));

        for (Thread t : threads) {
            t.join();
        }

        assertEquals(NREPS*NTHREADS, counter);
    }

    private AtomicInteger atomic_counter = new AtomicInteger(0);

    @Test
    public void incrementCounterMultiThreadedWithAtomicSynchTest()
            throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int i=0; i < NTHREADS; ++i) {
            Thread t = new Thread( () -> {
                for(int j= 0; j < NREPS; ++j) {
                    atomic_counter.getAndIncrement();
                }
            });
            t.start();
            threads.add(t);
        }

        // threads.forEach(t -> join(t));

        for (Thread t : threads) {
            t.join();
        }

        assertEquals(NREPS*NTHREADS, atomic_counter.get());
    }
}
