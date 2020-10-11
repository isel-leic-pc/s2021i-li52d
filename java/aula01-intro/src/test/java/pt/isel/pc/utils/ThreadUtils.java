package pt.isel.pc.utils;

public class ThreadUtils {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch(InterruptedException e) {

        }
    }

    public static void join(Thread t) {
        try {
            t.join();
        }
        catch(InterruptedException e) {

        }
    }
}
