package pt.isel.pc.monitors;

import pt.isel.pc.utils.NodeList;
import pt.isel.pc.utils.TimeoutHolder;

import java.sql.Time;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BroadcastEvent {
    private Lock monitor;
    private Condition awakeAll;

    private static class Request {
        public boolean done;

        public Request() {
            this.done = false;
        }
    }
    private NodeList<Request> requests;

    public BroadcastEvent() {
        monitor = new ReentrantLock();
        awakeAll = monitor.newCondition();
        requests = new NodeList<>();
    }

    /**
     * allways block the caller
     * until a event broadcast is done
     * @param timeout
     */
    public void await(long timeout)
            throws TimeoutException, InterruptedException {
        monitor.lock();
        try {
            if (timeout == 0)
                throw new TimeoutException();
            TimeoutHolder th = new TimeoutHolder(timeout);
            Request req = new Request();
            NodeList.Node<Request> node = requests.addLast(req);

            do {
                try {
                    awakeAll.await(th.remaining(),TimeUnit.MILLISECONDS);
                    if (req.done) {
                        return;
                    }
                    if (th.timeout()) {
                        requests.remove(node);
                        throw new TimeoutException();
                    }
                }
                catch(InterruptedException e) {
                    if (req.done) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    requests.remove(node);
                    throw e;
                }
            }
            while (true);
        }
        finally {
            monitor.unlock();
        }
    }

    /**
     * awakes all the blocked threads
     * but all future await operations
     * will block the caller until next broadcast
     */
    public void broadcast() {
        monitor.lock();
        try {
            for(Request req: requests)
                req.done = true;
            requests.clear();
            awakeAll.signalAll();
        }
        finally {
            monitor.unlock();
        }
    }
}
