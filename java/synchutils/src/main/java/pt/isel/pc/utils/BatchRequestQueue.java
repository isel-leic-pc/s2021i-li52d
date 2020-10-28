package pt.isel.pc.utils;

import jdk.jfr.Frequency;

public class BatchRequestQueue<T> {
    public static final class Request<T> {
        public  T value;
        public Request(T value) {
            this.value = value;
        }
    }

    private int count;

    private Request<T> current;

    public BatchRequestQueue(T t) {
        newBatch(t);
    }

    public BatchRequestQueue() {
        current = null;
        count = 0;
    }

    public Request<T> add() {
        count++;
        return current;
    }

    public void remove(Request<T> r) {
        if (count == 0 || r != current)
            throw new IllegalStateException();
        count--;
    }

    public void newBatch(T t) {
        current = new Request<T>(t);
        count = 0;
    }

    public int size() {
        return count;
    }

    public Request<T> current() {
        return current;
    }

    public void clear() {
        current = null;
        count = 0;
    }
}
