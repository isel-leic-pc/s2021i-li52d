package pt.isel.pc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.isel.pc.asyncio.AsyncFile;
import pt.isel.pc.asyncio.FileUtils;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;


public class AsyncFileTests {
	private static final Logger log =
		LoggerFactory.getLogger(AsyncFileTests.class);

	@Test
	public void copy2Files()
	throws TimeoutException, InterruptedException {

		String currDir = System.getProperty("user.dir");
		System.out.println(currDir);
		String fin1 = "fin1.dat";
		String fin2 = "fin2.dat";

		String fOut1 = "fout1.dat";
		String fOut2 = "fout2.dat";

		CountDownLatch cdl = new CountDownLatch(1);

		FileUtils.copy2Files(fin1, fOut1, fin2, fOut2, (e, l) -> {
			System.out.println("Copy done with " + l + " bytes!");
			cdl.countDown();
		});

		cdl.await();

	}

}
