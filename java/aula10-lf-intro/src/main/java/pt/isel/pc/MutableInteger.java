package pt.isel.pc;

public class MutableInteger {
    private int value;

    private int x;

    private void setX(int x) {
        this.x = x;
    }
    public  void set(int value) {
        setX(2);
        setX(3);
        synchronized (this) {
            this.value = value;
        }

    }

    public synchronized int get() {

        return value;
    }

}
