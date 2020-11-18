package pt.isel.pc;

import java.time.LocalTime;

public class Test {
    // necessary volatile to correct publication
    // of line 18 attribution
    public  volatile static Lazy<String> stringSupplier;

    public static void main(String[] args)
        throws InterruptedException{
        while (true) {
            stringSupplier = null;
            Runnable code = () -> {
                while (stringSupplier == null) ;
                System.out.println(stringSupplier.get());
            };
            Thread t1 = new Thread(code);
            t1.start();
            Thread t2 = new Thread(code);
            t2.start();
            stringSupplier = new Lazy<>(() -> LocalTime.now().toString());

            t1.join();
            t2.join();
        }
    }
}
