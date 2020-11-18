package pt.isel.pc;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class Lazy<T> {
    private  T value;
    private final Lock lock;
    private final Supplier<T> creator;

    public Lazy(Supplier<T> creator) {
        this.creator = creator;
        this.lock = new ReentrantLock();
    }

    public T get() {
        if (value == null) {
            lock.lock();
            try {
                if (value == null)
                    value = creator.get();
            }
            finally {
                lock.unlock();
            }
        }
        return value;
    }
}
