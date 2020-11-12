package pt.isel.pc;

public class Problematic {
    private static volatile boolean ready;
    private static int number;

    public static void main(String[] args)
        throws InterruptedException {
        System.out.println("Start!");

        Thread t1 = new Thread(() -> {
            while(!ready);
            System.out.println(number);
        });

        t1.start();
        // give thread time to start - ugly :(
        Thread.sleep(1000);

        number = 42;
        ready = true;

        t1.join();
        System.out.println("Done!");
    }
}