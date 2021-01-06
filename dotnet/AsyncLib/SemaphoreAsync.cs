using System;
using System.Threading.Tasks;
using System.Threading;
using System.Collections.Generic;


namespace AsyncLib {
    public class SemaphoreAsync {
     
        private int permits;
        private readonly int maxPermits;
        private readonly LinkedList<PendingAcquire> pendingAcquires;
        private readonly object mutex;

      

        // tasks
      
        private class PendingAcquire  {
           
        }

      
        public SemaphoreAsync(int initialPermits, int maxPermits) {
            if (initialPermits < 0 || initialPermits > maxPermits)
                throw new ArgumentException("Bad initial permits");
            mutex = new object();
            this.maxPermits = maxPermits;
            permits = initialPermits;
            pendingAcquires = new LinkedList<PendingAcquire>();
        }
    

        public  Task<bool> AcquireAsync(int units, CancellationToken ctoken, int timeout) {
            lock (mutex) {

            }
            return Task.FromResult(false);
               
        }

        public Task<bool> AcquireAsync(int units, CancellationToken ctoken) {
            return AcquireAsync(units, ctoken, Timeout.Infinite);

        }


        public void Release(int units) {
            if (permits + units < 0 || permits + units > maxPermits)
                throw new ArgumentException("Invalid release units");

            lock(mutex) {

			}
        }
    }
}
