package pt.isel.pc;

import pt.isel.pc.utils.BatchRequestQueue;
import pt.isel.pc.utils.NodeList;
import pt.isel.pc.utils.TimeoutHolder;

import javax.print.DocFlavor;
import javax.swing.text.html.Option;
import java.sql.Time;
import java.util.Optional;

public class MessageBoard<M> {
    private Object monitor;


    private NodeList<M> requests;


    private long expirationTime;
    private M message;

    public MessageBoard() {
        monitor = new Object();
        requests = new NodeList<>();
    }

    private void notiFyNewMessage(M message) {
        while(!requests.empty()) {
            NodeList.Node<M> node = requests.removeFirstNode();
            node.value = message;

        }
        monitor.notifyAll();
    }
    
    public void Publish(M message, int exposureTime) {
        synchronized(monitor) {
            if (!requests.empty()) {
                notiFyNewMessage(message);
            }
            if (exposureTime > 0) {
                expirationTime = System.currentTimeMillis() + exposureTime;
                this.message = message;
            }
        }
    }

    private boolean validMessage() {
        return message != null && System.currentTimeMillis() < expirationTime;
    }

    public Optional<M> Consume(long timeout)
            throws  InterruptedException {
        synchronized(monitor) {
            // non blocking path
            if (validMessage()) return Optional.of(message);
            // try path
            if (timeout == 0) return Optional.empty();
            // prepare blocking path
            TimeoutHolder th = new TimeoutHolder(timeout);
            NodeList.Node<M> node = requests.addLast(null);
            do {
                try {
                    monitor.wait(th.remaining());
                    if (node.value != null)
                        return Optional.of(node.value);
                    if (th.timeout()) {
                        requests.remove(node);
                        return Optional.empty();
                    }
                }
                catch(InterruptedException e) {
                    if (node.value != null) {
                        Thread.currentThread().interrupt();
                        return Optional.of(node.value);
                    }
                    requests.remove(node);
                    throw e;
                }
            } while(true);
        }
    }

}
