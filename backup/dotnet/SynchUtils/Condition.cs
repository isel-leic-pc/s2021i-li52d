using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;

namespace SemaphoreFifoED {
    public static class Condition {

        public static void EnterUninterruptibly(object monitor, out bool interrupted) {
            interrupted = false;

            do {
                try {
                    Monitor.Enter(monitor);
                    return;
                }
                catch (ThreadInterruptedException) {
                    interrupted = true;
                }
            }
            while (true);
        }

        /// <summary>
        ///  usage
        ///  condition.Wait(monitor, timeout)
        /// </summary>
        /// <param name="condition"></param>
        /// <param name="monitor"></param>
        /// <param name="timeout"></param>
        public static void Wait(this Object condition, Object monitor, int timeout) {
            if (monitor == condition) {
                Monitor.Wait(monitor, timeout);
                return;
            }


            Monitor.Enter(condition);
            Monitor.Exit(monitor);


            try {
                Monitor.Wait(condition, timeout);
            }
            finally {
                Monitor.Exit(condition);
                bool interrupted;
                EnterUninterruptibly(monitor, out interrupted);
                if (interrupted)
                    throw new ThreadInterruptedException();
            }

        }

        /// <summary>
        /// usage:
        /// condition.Notify(monitor);
        /// </summary>
        /// <param name="condition"></param>
        /// <param name="monitor"></param>
        public static void Notify(this Object condition, Object monitor) {
            if (condition == monitor) {
                Monitor.Pulse(monitor);
                return;
            }
            bool interrupted;
            EnterUninterruptibly(condition, out interrupted);
            Monitor.Pulse(condition);
            Monitor.Exit(condition);

            if (interrupted)
                Thread.CurrentThread.Interrupt();

        }

        public static void NotifyAll(this Object condition, Object monitor) {
            if (condition == monitor) {
                Monitor.PulseAll(monitor);
                return;
            }
            bool interrupted;
            EnterUninterruptibly(condition, out interrupted);
            Monitor.PulseAll(condition);
            Monitor.Exit(condition);

            if (interrupted)
                Thread.CurrentThread.Interrupt();
        }
    }
}
