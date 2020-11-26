package pt.isel.pc;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FixedSizeThreadPoolTests {
	private static final Logger log =
		LoggerFactory.getLogger(FixedSizeThreadPoolTests.class);

	@Test
	public void simpleSubmissionTest()
		throws InterruptedException, ExecutionException {

		FixedSizeThreadPool tp =
			new FixedSizeThreadPool(1);

		Future<Integer> fut = tp.submit(
				() ->  {
					log.info("Starting test on thread '{}'",
						Thread.currentThread().getName());

					return 2;
				}
		);

		Assert.assertEquals(PCFuture.class, fut.getClass() );
		Assert.assertEquals(2, fut.get().longValue());
	}
}
