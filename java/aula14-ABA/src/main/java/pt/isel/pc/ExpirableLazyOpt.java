package pt.isel.pc;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ExpirableLazyOpt<T>   {
    private static final int
                UNAVAILABLE = 0,
                BUSY = 1,
                AVAILABLE = 0x40000000;

    // configuration parameters
    private final Supplier<T> provider;
    private final long timeToLive;

    private final Object mon;

    private int available_version;
    private AtomicInteger state;
    private volatile T value;

    private long expirationTime;


    public ExpirableLazyOpt(Supplier<T> provider, long timeToLive) {
        this.mon = new Object();

        this.provider = provider;
        this.timeToLive = timeToLive;
        this.state.set(UNAVAILABLE);
        this.available_version = 0;
    }

    private void setAvailable(T val) {
        value = val;
        expirationTime =  timeToLive + System.currentTimeMillis();
        available_version =
            (available_version +1) == AVAILABLE ? 0 : available_version +1;
        // generate a new version of available state in order
        // to avoid the ABA problem
        state.set(AVAILABLE | available_version);
    }

    private boolean isAvailable(int obsState) {
        // note that in order to observe the AVAILABLE state
        // we must remove the version bits
        return (obsState & AVAILABLE) == AVAILABLE &&
            System.currentTimeMillis() > expirationTime;
    }

    private boolean TrySetBusy(int obsState) {
        return obsState != BUSY
            && state.compareAndSet(obsState, BUSY);
    }

    public T get() throws InterruptedException {
        T result;
        if ((result = tryGetValueOrMarkBusy()) != null)
            // a value is available
            return result;

        // successfully marked as busy, so try to compute the value.
        // here we assume in a simplistic way that the supplier never throws exceptions
        result = provider.get();

        // there is no optimization here to avoid unnecessary notifications
        synchronized(mon) {
            setAvailable(result);
            mon.notifyAll();
            return result;
        }
    }

    private T tryGetValueOrMarkBusy() throws InterruptedException {
        int obsState = state.get();
        // observe current value to guarantee that we have a value
        // consistent with the observed state
        T val = value;
        if (obsState == state.get() && isAvailable(obsState))
            return val;

        if (TrySetBusy(obsState))
            // this thread gained the right to call provider
            return null;

        synchronized(mon) {
            // another simplification here, because we don't catch
            // interrupted exceptions.
            // if we considered that the supplier may fail
            // we can have been chosen has the new generator
            // and in case of being interrupted, we
            // should notify another waiter in this case
            do {
                obsState = state.get();
                if (isAvailable(obsState)) return value;
                if (TrySetBusy(obsState))
                    // this thread gained the right to call provider
                    return null;
                mon.wait();
            } while (true);
        }
    }
}