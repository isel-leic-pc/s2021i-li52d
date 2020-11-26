package pt.isel.pc;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class PCFutureTests {
	private static final Logger log =
		LoggerFactory.getLogger(PCFutureTests.class);

	@Test
	public void simpleCompletionTest()
		throws InterruptedException, ExecutionException {
		Future<Integer> fut =
			PCFuture.submit(
				() ->  {
					log.info("Starting test on thread '{}'",
						Thread.currentThread().getName());

					return 2;
				}
			);

		Assert.assertNotEquals(null, fut.get() );
		Assert.assertEquals(2, fut.get().longValue());
	}
}
