package pt.isel.pc;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static pt.isel.pc.utils.ThreadUtils.sleep;

public class PCFutureOptTests {
	private static final Logger log =
		LoggerFactory.getLogger(PCFutureOptTests.class);

	@Test
	public void simpleCompletionTest()
		throws InterruptedException, ExecutionException {
		Future<Integer> fut =
			PCFutureOpt.submit(
				() ->  {
					log.info("Starting test on thread '{}'",
						Thread.currentThread().getName());

					return 2;
				}
			);

		Assert.assertNotEquals(null, fut.get() );
		Assert.assertEquals(2, fut.get().longValue());
	}

	@Test(expected = CancellationException.class)
	public void cancelANewFutureTest() {

		Future<Integer> f = new PCFutureOpt<>(() -> 1);

		new Thread(()-> {
			sleep(3000);
			f.cancel(false);
		}).start();

		try {
			f.get();
		}
		catch(InterruptedException | ExecutionException e) {

		}
	}

	@Test(expected = ArithmeticException.class)
	public void ThrowExceptionFutureTest()
		throws Throwable {

		Future<Integer> f =
			PCFutureOpt.submit(
				() ->  {
					log.info("Starting test on thread '{}'",
						Thread.currentThread().getName());
					sleep(3000);
					return 1 / 0;
				}
			);

		try {
			f.get();
		}
		catch(Exception e) {
			if (e instanceof ExecutionException)
				throw e.getCause();
		}
	}
}
