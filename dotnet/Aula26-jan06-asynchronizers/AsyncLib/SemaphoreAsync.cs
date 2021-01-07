using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace AsyncLib
{
	/// <summary>
	/// Final version of SemaphoreAsync (started as SimpleSemaphoreAsync)
	/// </summary>
	public class SemaphoreAsync
	{
		
		private int permits;
		private readonly int maxPermits;
		private readonly object mutex;

		// Task objects pre builded to avoid object creation per Acquire operation
		private static Task<bool> trueTask = Task.FromResult(true);
		private static Task<bool> timeoutTask = Task.FromException<bool>(new TimeoutException("timeout!"));

		// handlers for cancellation and timeout
		private Action<object> cancelHandler;
		private TimerCallback timeoutHandler;

		/// <summary>
		/// Represents a pending acquire operation
		/// </summary>
		private class PendingAcquire : IDisposable
		{
			internal readonly int units;
			private readonly TaskCompletionSource<bool> tcs;
			private Timer timer;
			private CancellationTokenRegistration regToken;
			
			private bool completed;
			private int isDisposed;

			internal PendingAcquire(int units) {
				this.units = units;
				tcs = new TaskCompletionSource<bool>();
			}

			internal bool Completed
			{
				get => completed;
				set => completed = true;
			}

			/// <summary>
			/// Complete with success
			/// </summary>
			internal void SetResult() {
				tcs.SetResult(true);
				Dispose();
			}

			/// <summary>
			/// Complete with timeout exception
			/// </summary>
			internal void SetTimeout() {
				tcs.SetException(new TimeoutException("timeout!"));
				Dispose();
			}

			/// <summary>
			/// Complete in canceled state
			/// </summary>
			internal void SetCanceled() {
				tcs.SetCanceled();
				// the registration will be disposed on callback return, so  avoid a new redundant dispose 
				regToken = default(CancellationTokenRegistration);
				Dispose();
			}

			internal Task<bool> Task { get => tcs.Task;  }

			internal Task<bool> Start(Timer timer, CancellationTokenRegistration regToken) {
				this.timer = timer;
				this.regToken = regToken;
				return Task;
			}

			public void Dispose() {
				if (Interlocked.CompareExchange(ref isDisposed, 1, 0) == 0) {
					timer?.Dispose();
					if (regToken != default(CancellationTokenRegistration))
						regToken.Dispose();
				}
			}
		}

		/// <summary>
		/// A possible (incomplete) alternative of PendingAcquire viewed 
		/// as a TaskCompletionSource specialization
		/// Not used
		/// </summary>
		private class PendingAcquire2 : TaskCompletionSource<bool>
		{
			internal readonly int units;

			internal PendingAcquire2(int units) : base() {
				
				this.units = units;
				 
			}

			internal void Complete() {
				SetResult(true);
			}
 
		}

		// pending acquires list
		private readonly LinkedList<PendingAcquire> pendingAcquires;

		/// <summary>
		/// Auxiliary method trying to satisfy pending acquires,
		/// called on a Release or cancellation/timeout operations
		/// </summary>
		/// <returns></returns>
		private LinkedList<PendingAcquire> trySatisfyPendingAcquires() {
			LinkedList<PendingAcquire> satisfiedAcquires = null;

			while (pendingAcquires.Count > 0 && permits >= pendingAcquires.First.Value.units) {
				PendingAcquire pa = pendingAcquires.First.Value;
				permits -= pa.units;
				pendingAcquires.RemoveFirst();
				if (satisfiedAcquires == null)
					satisfiedAcquires = new LinkedList<PendingAcquire>();
				satisfiedAcquires.AddLast(pa);
			}
			return satisfiedAcquires;
		}

		/// <summary>
		/// used to complete out of the lock the satis fied acquires returned by  "trySatisfyPendingAcquires"
		/// </summary>
		/// <param name="satisfiedAcquires"></param>
		private void commitSatisfiedAcquires(LinkedList<PendingAcquire> satisfiedAcquires) {
			if (satisfiedAcquires != null) {
				foreach (PendingAcquire pa in satisfiedAcquires) {
					pa.SetResult();
				}
			}
		}
		
		/// <summary>
		/// Used to resolve cancellation/timeout ocurrences
		/// </summary>
		/// <param name="node"></param>
		/// <param name="isCancelling"></param>
		private void CancellationHandler(
			LinkedListNode<PendingAcquire> node, bool isCancelling) {
			PendingAcquire pa = node.Value;
			bool toComplete = false;
			LinkedList<PendingAcquire> satisfiedAcquires = null;

			lock (mutex) {
				if (pa.Completed) return;
				pa.Completed = toComplete = true;
				pendingAcquires.Remove(node);
				satisfiedAcquires = trySatisfyPendingAcquires();
			}

			if (toComplete) {
				if (isCancelling)
					pa.SetCanceled();
				else
					pa.SetTimeout();

				commitSatisfiedAcquires(satisfiedAcquires);
			}
		}

		public SemaphoreAsync(int initialPermits, int maxPermits) {
			if (initialPermits < 0 || initialPermits > maxPermits)
				throw new ArgumentException("Bad initial permits");
			this.permits = initialPermits;
			this.maxPermits = maxPermits;
			this.mutex = new object();
			pendingAcquires = new LinkedList<PendingAcquire>();
			cancelHandler = (o) => CancellationHandler((LinkedListNode<PendingAcquire>)o, true);
			timeoutHandler = (o) => CancellationHandler((LinkedListNode<PendingAcquire>)o, false);
		}


		public Task<bool> AcquireAsync(int units) {
			return AcquireAsync(units, CancellationToken.None, Timeout.Infinite);
		}

		public Task<bool> AcquireAsync(int units, CancellationToken cToken) {
			return AcquireAsync(units, cToken, Timeout.Infinite);
		}

		public Task<bool> AcquireAsync(int units, int timeout) {
			return AcquireAsync(units, CancellationToken.None, timeout);
		}

		public Task<bool> AcquireAsync(int units, CancellationToken cToken, int timeout) {
			lock (mutex) {
				if (pendingAcquires.Count == 0 && permits >= units) {
					permits -= units;
					return trueTask;
				}
				if (timeout == 0)
					return timeoutTask;
				if (cToken.CanBeCanceled && cToken.IsCancellationRequested)
					return Task.FromCanceled<bool>(cToken);

				PendingAcquire pa = new PendingAcquire(units);
				LinkedListNode<PendingAcquire> node = pendingAcquires.AddLast(pa);

				CancellationTokenRegistration regToken =
					default(CancellationTokenRegistration);
				Timer timer = null;
				if (cToken.CanBeCanceled)
					regToken = cToken.Register(cancelHandler, node);
				if (timeout != Timeout.Infinite)
					timer = new Timer(timeoutHandler, node, timeout, Timeout.Infinite);

				// save regToken and timer on PendingAcquire for future eventual disposing
				// and return the operation task
				return pa.Start(timer, regToken);
			}
		
		}

		public void Release(int units) {
		
			if (permits + units < 0 || permits + units > maxPermits)
				throw new ArgumentException("Invalid release units");
			LinkedList<PendingAcquire> satisfiedAcquires = null;

			lock (mutex) {
				permits += units;
				satisfiedAcquires = trySatisfyPendingAcquires();
			}

			commitSatisfiedAcquires(satisfiedAcquires);
		}
	}
}
