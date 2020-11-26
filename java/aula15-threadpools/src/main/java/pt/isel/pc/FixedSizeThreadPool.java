package pt.isel.pc;

import java.util.concurrent.*;

public class FixedSizeThreadPool extends ThreadPoolExecutor {
	private static final long keepAliveTime = 60;

	public FixedSizeThreadPool(int corePoolSize) {
		super(corePoolSize, corePoolSize,
			keepAliveTime, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>());
	}

	@Override
	protected  <V> RunnableFuture<V> newTaskFor(Callable<V> supplier) {
		return new PCFuture<V>(supplier);
	}
}
