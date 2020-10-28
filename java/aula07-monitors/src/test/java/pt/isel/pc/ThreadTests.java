package pt.isel.pc;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isel.pc.monitors.CounterSemaphoreSN;
import pt.isel.pc.monitors.CounterSemaphoreSNTests;

import java.util.concurrent.CountDownLatch;

public class ThreadTests {
    private static final Logger log =
            LoggerFactory.getLogger(ThreadTests.class);

    @Test
    public void assert_on_foreign_thread_test() {
     
        Thread t = new Thread(() -> {
            log.info("Printed after fail");
        });

        t.start();

        log.info("In Thread Test!");
    }
}
