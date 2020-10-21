package pt.isel.pc.utils;

public class TimeoutHolder {
    private long toExpire;
    private boolean withTimeout;
    public static final long INFINITE = -1;

    public TimeoutHolder(long millis) {
        if (millis == INFINITE) withTimeout = false;
        else {
            toExpire = System.currentTimeMillis() + millis;
            withTimeout = true;
        }
    }
 
    public long remaining() {
        if (!withTimeout) return Long.MAX_VALUE;
        return Math.max(0, toExpire - System.currentTimeMillis());
    }

    public boolean timeout() {
        return remaining() == 0;
    }
}
