package pt.isel.pc;

import pt.isel.pc.utils.NodeList;
import pt.isel.pc.utils.TimeoutHolder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BroadcastEvent {
    private Lock monitor;
    private Condition condition;

    private static class Request {
        public boolean done;
    }

    private NodeList<Request> requests;

    public BroadcastEvent() {
        monitor = new ReentrantLock();
        condition = monitor.newCondition();
        requests = new NodeList<>();
    }

    /**
     * allways block the caller
     * until a event broadcast is done
     * @param timeout
     */
    public void await(long timeout)
            throws TimeoutException, InterruptedException{
        monitor.lock();
        try {
            if (timeout == 0)
                throw new TimeoutException();
            TimeoutHolder th = new TimeoutHolder(timeout);
            Request req = new Request();
            NodeList.Node<Request> node = requests.addLast(req);
            do {
                try {
                    condition.await(th.remaining(),
                            TimeUnit.MILLISECONDS);
                    if (req.done) return;
                    if (th.timeout()) {
                        requests.remove(node);
                        throw new TimeoutException();
                    }
                }
                catch(InterruptedException e) {
                    if (req.done) {
                        Thread.currentThread().interrupt();
                        requests.remove(node);
                        return;
                    }
                    requests.remove(node);
                    throw e;
                }
            }
            while(true);
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
            for ( Request req : requests) {
                req.done = true;
            }
            condition.signalAll();
            requests.clear();
        }
        finally {
            monitor.unlock();
        }
    }
}
