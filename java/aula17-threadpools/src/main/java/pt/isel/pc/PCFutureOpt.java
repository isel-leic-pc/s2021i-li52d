package pt.isel.pc;

import pt.isel.pc.utils.TimeoutHolder;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PCFutureOpt<V> implements Future<V> {
	/// states

	private static final int NEW        = 1;
	private static final int STARTED    = 2;
	private static final int COMPLETED  = 4;
	private static final int CANCELLED  = 5;
	private static final int ERROR      = 6;

	private AtomicInteger state;
	private Callable<V> supplier;
	private volatile Thread thread;
	private volatile V value;
	private Exception exception;

	private volatile int waiters;

	private Object mon;


	public static <V> Future<V> submit(Callable<V> supplier) {
		// just for test purposes
		PCFutureOpt<V> future = new PCFutureOpt<V>(supplier);
		(new Thread( future::run)).start();
		return future;
	}

	public  PCFutureOpt(Callable<V> supplier) {
		this.supplier = supplier;
		mon = new Object();
		state = new AtomicInteger(NEW);
	}


	public void run() {

		int obsState = state.get();
		thread = Thread.currentThread();
		if (obsState != NEW || !state.compareAndSet(obsState, STARTED))
			return;

		V val = null;
		Exception exc = null;
		int s;
		try {
			val = supplier.call();
			s = COMPLETED;
		} catch (InterruptedException e) {
			s = CANCELLED;
		} catch (Exception e) {
			s = ERROR;
			exc = e;
		}

		if (s == COMPLETED) value = val;
		else if (s == ERROR) exception = exc;

		//if (state.get() != CANCELLED) state.set(s);
		//just don't do anything to the aboved commented code
		//IT LACKS ATOMICITY!
		obsState = state.get();
		if (obsState != CANCELLED && state.compareAndSet(obsState, s)) {
			if (waiters > 0) {
				synchronized (mon) {
					if (waiters > 0)
						mon.notifyAll();
				}
			}
		}
	}


	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		int obsState = state.get();
		if (obsState > STARTED  ||
			!state.compareAndSet(obsState, CANCELLED) ) return false;

		if (obsState != NEW && mayInterruptIfRunning)
			thread.interrupt();

		if (waiters > 0) {
			synchronized (mon) {
				if (waiters > 0)
					mon.notifyAll();
			}
		}
		return true;
	}

	@Override
	public boolean isCancelled() {
		return state.get() == CANCELLED;
	}

	@Override
	public boolean isDone() {
		return state.get() > STARTED;
	}

	private boolean processCompletion()
		throws ExecutionException {
		int obsState = state.get();
		if (obsState == COMPLETED)
			return true;
		else if (obsState == ERROR)
			throw new ExecutionException(exception);
		else if (obsState == CANCELLED)
			throw new CancellationException();
		else
			return false;
	}

	@Override
	public V get(long timeout, TimeUnit unit)
		throws  InterruptedException,
		ExecutionException,
		TimeoutException {

		if (processCompletion()) return value;

		// We must (eventually) wait
		synchronized (mon) {

			if (timeout == 0)
				throw new TimeoutException();
			TimeoutHolder th = new TimeoutHolder(timeout);
			waiters++;
			do {
				try {
					if (processCompletion()) return value;
					if (th.timeout()) throw new TimeoutException();
					mon.wait(th.remaining());
				}
				finally {
					waiters--;
				}
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

