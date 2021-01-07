using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace AsyncLib
{
	public class SimpleSemaphoreAsync
	{
		
		private int permits;
		private readonly int maxPermits;
		private readonly object mutex;

		private Action<object> cancelHandler;
		private TimerCallback timeoutHandler;

		private class PendingAcquire : IDisposable
		{
			internal readonly int units;
			private readonly TaskCompletionSource<bool> tcs;
			Timer timer;
			CancellationTokenRegistration regToken;
			private bool completed;

			internal PendingAcquire(int units) {
				this.units = units;
				tcs = new TaskCompletionSource<bool>();
			}

			internal void SetResult() {
				tcs.SetResult(true);
				Dispose();
			}

			internal void SetTimeout() {
				tcs.SetException(new TimeoutException("timeout!"));
				Dispose();
			}

			internal void SetCanceled() {
				tcs.SetCanceled();
				Dispose();
			}

			internal bool Completed
			{
				 get =>  completed;
				 set => completed = true;
			}

			internal Task<bool> Task { get => tcs.Task;  }

			internal Task<bool> Start(Timer timer, CancellationTokenRegistration regToken) {
				this.timer = timer;
				this.regToken = regToken;
				return Task;
			}

			public void Dispose() {
				timer?.Dispose();
				if (regToken != default(CancellationTokenRegistration))
					regToken.Dispose();
			}
		}

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

		private readonly LinkedList<PendingAcquire> pendingAcquires;

		private static Task<bool> trueTask = Task.FromResult(true);
		private static Task<bool> timeoutTask = Task.FromException<bool>(new TimeoutException("timeout"));
		

		private void CancellationHandler(LinkedListNode<PendingAcquire> node,
										bool isCancelled) {
			bool toComplete = false;
			PendingAcquire pa = null;
			lock (mutex) {
			    pa = node.Value;
				if (pa.Completed) return;
				pa.Completed = toComplete = true;
				pendingAcquires.Remove(node);
			}
			if (toComplete) { 
				if (isCancelled)
					pa.SetCanceled();
				else
					pa.SetTimeout();
			}
		}

		public SimpleSemaphoreAsync(int initialPermits, int maxPermits) {
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
			lock(mutex) {
				if (pendingAcquires.Count == 0 && permits >= units) {
					permits -= units;
					return trueTask;
				}
				PendingAcquire pa = new PendingAcquire(units);
				pendingAcquires.AddLast(pa);
				return pa.Task;
			}
		}

	

		public Task<bool> AcquireAsync(int units, 
									   CancellationToken cToken,
									   int timeout) {

			
			lock (mutex) {
				if (pendingAcquires.Count == 0 && permits >= units) {
					permits -= units;
					return trueTask;
				}
				if (timeout == 0) {
					return timeoutTask;
				}
				if (cToken.CanBeCanceled && cToken.IsCancellationRequested) {
					return Task.FromCanceled<bool>(cToken);
				}

				PendingAcquire pa = new PendingAcquire(units);
				LinkedListNode<PendingAcquire> node = 
					pendingAcquires.AddLast(pa);

				CancellationTokenRegistration regHandler =
					default(CancellationTokenRegistration);

				Timer t = null;
				if (cToken.CanBeCanceled)
					regHandler = cToken.Register(cancelHandler, node);
				if (timeout != Timeout.Infinite)
					t = new Timer(timeoutHandler, node, timeout, Timeout.Infinite);

				return pa.Start(t, regHandler);
			}
		}

		public void Release(int units) {
		
			if (permits + units < 0 || permits + units > maxPermits)
				throw new ArgumentException("Invalid release units");
			permits += units;
			LinkedList<PendingAcquire> satisfiedRequests = null;

			lock (mutex) {
				while(pendingAcquires.Count > 0 && permits >= pendingAcquires.First.Value.units) {
					PendingAcquire pa = pendingAcquires.First.Value;
					permits -= pa.units;
					pendingAcquires.RemoveFirst();
					pa.Completed = true;
					if (satisfiedRequests == null)
						satisfiedRequests = new LinkedList<PendingAcquire>();
					satisfiedRequests.AddLast(pa);
				}
			}

			if (satisfiedRequests == null) return;
			foreach(PendingAcquire pa in satisfiedRequests) {
				pa.SetResult();
			}
			
		}
	}
}
