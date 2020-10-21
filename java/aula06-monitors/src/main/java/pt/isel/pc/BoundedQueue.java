package pt.isel.pc;

import pt.isel.pc.utils.TimeoutHolder;

import javax.management.RuntimeOperationsException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static pt.isel.pc.utils.TimeoutHolder.INFINITE;

public class BoundedQueue<T> {
    private Lock monitor = new ReentrantLock();
    private Condition hasSpace, hasItems;
    List<T> items;
    private int maxItems;

    public BoundedQueue(int maxItems) {
        this.maxItems = maxItems;
        items = new LinkedList<>();
        hasSpace = monitor.newCondition();
        hasItems = monitor.newCondition();
    }

    public void put(T item) throws InterruptedException{
        monitor.lock();
        try {
            do {
                try {
                    while (items.size() == maxItems)
                        hasSpace.await();
                    items.add(item);
                    hasItems.signal();
                }
                catch(InterruptedException e) {
                    if (items.size() < maxItems)
                        hasSpace.signal();
                    throw e;
                }
            }
            while(true);
        }
        finally {
            monitor.unlock();
        }
    }

    public T get(long timeout)
            throws InterruptedException, TimeoutException{
        monitor.lock();
        try {
            if (items.size() > 0) {
                T item = items.remove(0);
                hasSpace.signal();
                return item;
            }
            if (timeout == 0)
                throw new TimeoutException();
            TimeoutHolder th = new TimeoutHolder(timeout);
            do {
                try {
                    hasItems.await(th.remaining(), TimeUnit.MILLISECONDS);
                    if (items.size() > 0) {
                        T item = items.remove(0);
                        hasSpace.signal();
                        return item;
                    }

                    if (th.timeout())
                        throw new TimeoutException();
                }
                catch(InterruptedException e) {
                    if (items.size() > 0)
                        hasSpace.signal();
                    throw e;
                }
            }
            while (true);
        }
        finally {
            monitor.unlock();
        }
    }

}
