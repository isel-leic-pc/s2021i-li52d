import asyncio.FileUtils;
import io.reactivex.Single;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static utils.ThreadUtils.sleep;

public class AsyncIoTests {

	@Test
	public void copyWithObservable() throws IOException {
		long expectedSize = 13483;

		Single<Long> copyDone =
			FileUtils.copyFile("fin1.dat", "fout1.dat");

		/*
		copyDone.subscribe((l,t) -> {
			if (t != null) System.out.println("error: " + t);
			else System.out.println("size = " + l);
		});
		*/
		System.out.println("wait");

		Long res = copyDone.blockingGet();

		Assert.assertEquals(expectedSize, res.longValue());

		//sleep(5000);
	}
}
