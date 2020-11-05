package pt.isel.pc;

import pt.isel.pc.utils.NodeList;
import pt.isel.pc.utils.TimeoutHolder;

public class ManualResetEvent2 {
    private Object monitor = new Object();
    private boolean signaled;

    private static class Request {
        public  boolean done;
    }

    int current_version = 1;


    ManualResetEvent2(boolean initiaState) {
        signaled = initiaState;
    }

    public boolean await(long timeout)
            throws InterruptedException {
        synchronized (monitor) {
            // non blocking path
            if (signaled) return true;
            // try (no-wait) path
            if (timeout == 0) return false;

            // blocking path
            TimeoutHolder th = new TimeoutHolder(timeout);

            int version = current_version;

            do {
                monitor.wait(th.remaining());
                if (version != current_version) return true;
                if (th.timeout()) return false;
            }
            while(true);
        }
    }

    public void signal() {
        synchronized (monitor) {
            signaled = true;
            current_version++;
            monitor.notifyAll();
        }
    }

    public void reset() {
        synchronized (monitor) {
            signaled = false;
        }
    }
}
