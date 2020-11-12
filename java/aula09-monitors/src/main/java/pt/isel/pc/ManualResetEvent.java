package pt.isel.pc;


import pt.isel.pc.utils.NodeList;
import pt.isel.pc.utils.TimeoutHolder;

public class ManualResetEvent {
    private Object monitor = new Object();
    private boolean signaled;

    private static class Request {
        public  boolean done;
    }

    NodeList<Request> requests;

    ManualResetEvent(boolean initiaState) {
        signaled = initiaState;
        requests = new NodeList<Request>();
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
            Request req = new Request();
            NodeList.Node<Request> node = requests.addLast(req);

            do {
                try {
                    monitor.wait(th.remaining());
                    if (req.done) return true;
                    if (th.timeout()) {
                        requests.remove(node);
                        return false;
                    }
                }
                catch(InterruptedException e) {
                    if (req.done) {
                        Thread.currentThread().interrupt();
                        return true;
                    }
                    requests.remove(node);
                    throw e;
                }
            }
            while(true);
        }
    }

    public void signal() {
        synchronized (monitor) {
            signaled = true;
            for(Request r : requests) {
                r.done = true;
            }

            requests.clear();
            monitor.notifyAll();
        }
    }

    public void reset() {
        synchronized (monitor) {
            signaled = false;
        }
    }
}
