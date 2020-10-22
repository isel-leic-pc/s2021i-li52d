

using SynchUtils;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;


namespace SemaphoreFifoED
{
    public class SemaphoreFifoED
    {
        private object monitor;
        private int permits;

        private class Request {
            public readonly int units;
            
            public Request(int units) {
                this.units = units;
            }

            public bool Processed {
                get; set;
            }
        }

        private LinkedList<Request> requests;


        private void notifyWaiters() {
            int total = 0;
            do {
                if (requests.Count == 0) break;
                LinkedListNode<Request> first = requests.First;
                if (permits < first.Value.units) break;
                permits -= first.Value.units;
                requests.RemoveFirst();
            }
            while (true);
            if (total > 0) Monitor.PulseAll(monitor);
        }

        public SemaphoreFifoED(int initialPermits) {
            monitor = new object();
            if (initialPermits > 0) permits = initialPermits;
            requests = new LinkedList<Request>();
        }

        public bool Acquire(int units, int timeout) {
            lock(monitor) {
                // non blocking path
                if (requests.Count == 0 && permits >= units) {
                    permits -= units;
                    return true;
                }

                // try path
                if (timeout == 0) return false;

                // prepare wait
                TimeoutHolder th = new TimeoutHolder(timeout);
                LinkedListNode<Request> node = requests.AddLast(new Request(units));

                do {
                    try {
                        Monitor.Wait(monitor, th.Remaining);
                        if (node.Value.Processed) return true;
                        if (th.Timeout) {
                            requests.Remove(node);
                            notifyWaiters();
                            return false;
                        }
                    }
                    catch(ThreadInterruptedException) {
                        if (node.Value.Processed) {
                            Thread.CurrentThread.Interrupt();
                            return true;
                        }
                        requests.Remove(node);
                        notifyWaiters();
                        throw;
                    }
                }
                while (true);
            
            }
        }

        public void Release(int units) {
            lock(monitor) {
                permits += units;
                notifyWaiters();
            }
        }
    }
}
