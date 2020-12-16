using System;
using System.Threading;

using SynchUtils;

namespace Experiments.Lock_free {
    class Semaphore {
        private Object monitor = new Object();

        private  volatile int permits;
        
        public Semaphore(int initial) {
            permits = initial;
        }

        private volatile int waiters;

        private bool tryAcquire() {
            int obsPermits;
            do {
                obsPermits = permits;
                if (obsPermits == 0) return false;
            }
            while (Interlocked.CompareExchange(ref permits, obsPermits - 1, obsPermits) != obsPermits);
            return true;
        }

        /**
         *  Acquire operation.
         * @param timeout max waiting time
         * @return  true if operation succeeded, false if timeout
         * @throws ThreadInterruptedException
         */
        public bool Acquire(int timeout) { //  throws InterruptedException  
            // fast path
            if (tryAcquire())
                return true;

            if (timeout == 0) // fail!
                return false;

            lock (monitor) {
                waiters++;

                // The following (complete) barrier is necessary on CLI to avoid reordering of the waiters write in line 43 
                // and the permits read on the execution of tryAcquire!
                Thread.MemoryBarrier();

                if (tryAcquire()) { // last try
                    waiters--;
                    return true;
                }
                try {
                    // prepare wait
                    TimeoutHolder th = new TimeoutHolder(timeout);
                    do {
                        int refTime = Environment.TickCount;
                        Monitor.Wait(monitor, th.Remaining);
                        if (tryAcquire()) return true;
                        if (th.Timeout) return false;
                    }
                    while (true);
                }
                catch (ThreadInterruptedException e) {
                    if (permits > 0) Monitor.Pulse(monitor);
                    throw e;
                }
                finally {
                    waiters--;
                }
               
            }
        }

        public void Acquire() {
            Acquire(Timeout.Infinite);
        }

        /**
        * Release  
        */
        public void Release() {
            Interlocked.Increment(ref permits);
            if (waiters > 0) { 
                lock (monitor) {
                    if (waiters > 0)
                        Monitor.Pulse(monitor);
                }
            }
        }
    }
}
