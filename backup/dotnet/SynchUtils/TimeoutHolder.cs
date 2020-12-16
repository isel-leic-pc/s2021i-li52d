using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SynchUtils {
    public class TimeoutHolder {
        private bool withTimeout;
        private int expired;
        public static readonly long INFINITE = -1;

        public TimeoutHolder(int millis) {
            if (millis == INFINITE) { withTimeout = false; }
            else { expired = Environment.TickCount + millis; withTimeout = true; }
        }

        

        public int Remaining {
            get {
                if (!withTimeout) return int.MaxValue;
                return Math.Max(0, expired - Environment.TickCount);
            }

        }

        public bool Timeout {
            get { return Remaining == 0; }
        }
    }
}
