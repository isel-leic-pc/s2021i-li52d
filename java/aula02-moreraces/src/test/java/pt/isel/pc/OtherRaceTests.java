package pt.isel.pc;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class OtherRaceTests {
    private static final int N_THREADS = 10;

    private static final int N_REPS = 4000000;
    private static final int N_KEYS = N_REPS;

    // first try - with a synchronized hash map from Java 7

    private Map<Integer, AtomicInteger> synchMap =
            Collections.synchronizedMap(new HashMap<>());

    private void synchMapCounter() {
        for (int key = 1; key <= N_KEYS; ++key) {
             AtomicInteger val;
              if ((val =synchMap.get(key)) == null) {
                  val = new AtomicInteger(1);
                  synchMap.put(key, val);
              }
              else val.incrementAndGet();
        }
    }

    @Test
    public void badCountWithSynchMapTest() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int t=0; t < N_THREADS; ++t) {
            Thread thr = new Thread(this::synchMapCounter);
            thr.start();
            threads.add(thr);
        }
        for (Thread thread : threads) {
            thread.join();
        }
        int totalCount = synchMap.values().stream()
                .map(AtomicInteger::get)
                .reduce(0, Integer::sum);
        assertNotEquals(N_THREADS * N_KEYS, totalCount);
    }


    // second try - with a customized hashmap for thread safe check and act

    private static class SynchCountersMap {
        private static class MutableInteger {
            private int val;
            public MutableInteger(int initial) {
                val = initial;
            }

            public int get() { return val; }

            public void increment() { ++val; }
        }

        private Map<Integer, MutableInteger> map = new HashMap<>();
        private Object lock = new Object();

        public void increment(int key) {
           synchronized (lock) {
               MutableInteger val;
               if ((val = map.get(key)) == null) {
                   val = new MutableInteger(1);
                   map.put(key, val);
               } else val.increment();
           }
        }
        synchronized public Collection<MutableInteger> values() { return map.values(); }
    }

    @Test
    public void countWithOurSynchMapTest() throws InterruptedException {
        SynchCountersMap counters = new SynchCountersMap();
        List<Thread> threads = new ArrayList<>();
        for (int t=0; t < N_THREADS; ++t) {
            Thread thr = new Thread(() -> {
                for (int key = 1; key <= N_KEYS; ++key) {
                       counters.increment(key);
                }
            });
            thr.start();
            threads.add(thr);
        }
        for (Thread thread : threads) {
            thread.join();
        }

        int totalCount = counters.values().stream()
                .map(mi -> mi.get())
               .reduce(0, Integer::sum);
        assertEquals(N_THREADS * N_KEYS, totalCount);
    }

    // third try - with a concurrentMap

    private Map<Integer, AtomicInteger> concurrentMap =
            new ConcurrentHashMap<>();

    private void concurrentMapCounter() {
        for (int key = 1; key <= N_KEYS; ++key) {

            concurrentMap
                    .computeIfAbsent(key, (__ -> new AtomicInteger() ))
                    .incrementAndGet();
        }
    }

    @Test
    public void countWithConcurrentMapTest() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int t=0; t < N_THREADS; ++t) {
            Thread thr = new Thread(this::concurrentMapCounter);
            thr.start();
            threads.add(thr);
        }
        for (Thread thread : threads) {
            thread.join();
        }

        int totalCount = concurrentMap.values().stream()
                .map(AtomicInteger::get)
                .reduce(0, Integer::sum);
        assertEquals(N_THREADS * N_KEYS, totalCount);
    }
}
