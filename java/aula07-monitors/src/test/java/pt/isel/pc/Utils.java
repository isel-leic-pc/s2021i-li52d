package pt.isel.pc;

public class Utils {
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch(InterruptedException e) {

        }
    }

    public static void join(Thread t, long millis) {
        do {
            try {
                t.join(millis);
                return;
            } catch (InterruptedException e) {

            }
        }
        while(true);
    }
}
