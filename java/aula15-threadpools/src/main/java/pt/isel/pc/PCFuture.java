package pt.isel.pc;

import pt.isel.pc.utils.TimeoutHolder;

import java.util.concurrent.*;

public class PCFuture<V> implements RunnableFuture<V>  {

	/// states

	private static final int NEW        = 1;
	private static final int STARTED    = 2;
	private static final int COMPLETED  = 4;
	private static final int CANCELLED  = 5;
	private static final int ERROR      = 6;

    private int state;              // production state

	private V value;                // the produced value
									// if state COMPLETED

	private Exception exception;    // thrown exception
									// if state ERROR

	private Callable<V> supplier;
    private Thread thread;          // the executing thread

    private Object mon;             // our monitor


	// Just for testing purposes
	public static <V> Future<V> submit(Callable<V> supplier) {

		PCFuture<V> future = new PCFuture<V>(supplier);
		(new Thread( future::run)).start();
		return future;
	}

	public  PCFuture(Callable<V> supplier) {
		this.supplier = supplier;
		mon = new Object();
		state = NEW;
	}


	public void run() {
		synchronized(mon) {
			if (state != NEW) return;
			thread = Thread.currentThread();
			state = STARTED;
		}
		V val = null;
		Exception exc = null;
		int s;
		try {
			val = supplier.call();
			s = COMPLETED;
		}
		catch(InterruptedException e) {
			s = CANCELLED;
		}
		catch(Exception e) {
			exc = e;
			s = ERROR;
		}
		synchronized(mon) {
			if (s == COMPLETED) value = val;
			else if (s == ERROR) exception = exc;
			if (state != CANCELLED) state = s;
			mon.notifyAll();
		}

	}


	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		synchronized (mon) {
			if (state > STARTED) {
				return false;
			}
			if (state != NEW && mayInterruptIfRunning)
				thread.interrupt();

			state = CANCELLED;
			return true;
		}
	}

	@Override
	public boolean isCancelled() {
		synchronized (mon) {
			return state == CANCELLED;
		}
	}

	@Override
	public boolean isDone() {
		synchronized (mon) {
			return state > STARTED;
		}
	}

	private boolean processCompletion()
		throws ExecutionException {
		if (state == COMPLETED)
			return true;
		else if (state == ERROR)
			throw new ExecutionException(exception);
		else if (state == CANCELLED)
			throw new CancellationException();
		else
			return false;
	}

	@Override
	public V get(long timeout, TimeUnit unit)
		throws  InterruptedException,
		        ExecutionException,
				TimeoutException {
		synchronized (mon) {
			// non waiting path
			if (processCompletion()) return value;
			if (timeout == 0)
				throw new TimeoutException();
			TimeoutHolder th = new TimeoutHolder(timeout);
			do {
				mon.wait(th.remaining());
				if (processCompletion()) return value;
				if (th.timeout()) throw new TimeoutException();
			}
			while(true);
		}
	}

	@Override
	public V get()
			throws  InterruptedException,
					ExecutionException {
		V val = null;
		try {
			val = get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		}
		catch(TimeoutException e) {

		}
		return val;
	}
}
