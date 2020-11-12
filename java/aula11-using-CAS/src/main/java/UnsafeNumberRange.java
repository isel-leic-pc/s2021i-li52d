

public class UnsafeNumberRange {
    // Must keep the invariant lower <= upper!
    private int lower;
    private int upper;

    public void setLower(int l) {
        if (l > upper)
            throw new IllegalArgumentException();
        lower = l;
    }

    public void setUpper(int u) {
        if (u < lower)
            throw new IllegalArgumentException();
        upper = u;
    }
}
