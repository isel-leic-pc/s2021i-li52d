import java.util.concurrent.atomic.AtomicInteger;

public class UnsafeNumberRange {
    // Must keep the invariant lower <= upper!
    private AtomicInteger lower = new AtomicInteger(0);
    private AtomicInteger  upper = new AtomicInteger(0);

    public void setLower(int l) {
        /*
        if (l > upper)
            throw new IllegalArgumentException();
        lower = l;
        */
        do {
           int obsUpper = upper.get();
           int obsLower = lower.get();
           if (l > obsUpper)
               throw new IllegalArgumentException();
           if (lower.compareAndSet(obsLower, l))
               return;
        }
        while(true);
    }

    public void setUpper(int u) {
        /*
        if (u < lower)
            throw new IllegalArgumentException();
        upper = u;

         */
        do {
            int obsUpper = upper.get();
            int obsLower = lower.get();
            if (u < obsLower)
                throw new IllegalArgumentException();
            if (upper.compareAndSet(obsUpper, u))
                return;
        }
        while(true);
    }
}
