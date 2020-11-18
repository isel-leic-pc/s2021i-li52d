import java.awt.dnd.InvalidDnDOperationException;
import java.util.concurrent.atomic.AtomicReference;

public class SafeNumberRange {

    private class RangeHolder {
        public final int lower;
        public final int upper;

        public RangeHolder(int lower, int upper) {
            this.lower = lower; this.upper = upper;
        }
    }

    private AtomicReference<RangeHolder> range =
            new AtomicReference<>(new RangeHolder(0,0));

    public void setLower(int l) {
        do {
            RangeHolder obs  = range.get();
            if (l > obs.upper )
                throw new IllegalArgumentException();
            if (range.compareAndSet(obs, new RangeHolder(l, obs.upper)))
                return;
        }
        while(true);
    }

    public void setUpper(int u) {

    }

}
