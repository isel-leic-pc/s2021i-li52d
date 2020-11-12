package pt.isel.pc;

import pt.isel.pc.utils.BatchRequestQueue;
import pt.isel.pc.utils.TimeoutHolder;

import javax.print.DocFlavor;
import javax.swing.text.html.Option;
import java.util.Optional;

public class MessageBoard<M> {
    private Object monitor;


    private BatchRequestQueue<M> batch;

    private long expirationTime;
    private M message;

    public MessageBoard() {
        batch = new BatchRequestQueue<>();
        monitor = new Object();
    }

    public void Publish(M message, int exposureTime) {
        synchronized(monitor) {
            if (exposureTime > 0) {
                expirationTime = System.currentTimeMillis() + exposureTime;
                this.message = message;
            }
            if (batch.size() > 0) {
                batch.current().value = message;
                monitor.notifyAll();
            }
            batch.newBatch(null);
        }
    }

    private boolean validMessage() {
        return message != null && expirationTime >
                System.currentTimeMillis();
    }

    public Optional<M> Consume(long timeout)
            throws  InterruptedException {
        synchronized(monitor) {
            if (validMessage()) return Optional.of(message);
            if (timeout == 0) return Optional.empty();
            BatchRequestQueue.Request<M> req = batch.add();
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                try {
                    monitor.wait(th.remaining());
                    if (req.value != null) return Optional.of(req.value);
                    if (th.timeout()) {
                        batch.remove(req);
                        return Optional.empty();
                    }
                }
                catch(InterruptedException e) {
                    if (req.value != null) {
                        Thread.currentThread().interrupt();
                        return Optional.of(req.value);
                    }
                    batch.remove(req);
                    throw e;
                }
            }
            while(true);
        }
    }

}
