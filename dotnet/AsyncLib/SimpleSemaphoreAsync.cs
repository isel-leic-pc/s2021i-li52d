using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;

namespace AsyncLib
{
	public class SimpleSemaphoreAsync
	{
		
		private int permits;
		private readonly int maxPermits;
		private readonly object mutex;

		private class PendingAcquire
		{
			internal readonly int units;
			private readonly TaskCompletionSource<bool> tcs;

			internal PendingAcquire(int units) {
				this.units = units;
				tcs = new TaskCompletionSource<bool>();
			}

			internal void Complete() {
				tcs.SetResult(true);
			}

			internal Task<bool> Task { get => tcs.Task;  }
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

		
		public SimpleSemaphoreAsync(int initialPermits, int maxPermits) {
			if (initialPermits < 0 || initialPermits > maxPermits)
				throw new ArgumentException("Bad initial permits");
			this.permits = initialPermits;
			this.maxPermits = maxPermits;
			this.mutex = new object();
			pendingAcquires = new LinkedList<PendingAcquire>();
		}

		public Task<bool> AcquireAsync(int units) {
			lock(mutex) {
				if (pendingAcquires.Count == 0 && permits >= units) {
					permits -= units;
					return Task.FromResult(true);
				}
				PendingAcquire pa = new PendingAcquire(units);
				pendingAcquires.AddLast(pa);
				return pa.Task;
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
					if (satisfiedRequests == null)
						satisfiedRequests = new LinkedList<PendingAcquire>();
					satisfiedRequests.AddLast(pa);
				}
			}

			if (satisfiedRequests == null) return;
			foreach(PendingAcquire pa in satisfiedRequests) {
				pa.Complete();
			}
			
		}
	}
}
