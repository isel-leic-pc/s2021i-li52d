using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using static BlockingQueue.Utils;

namespace AsyncLib
{
	public class BlockingQueueAsync<T>
	{
		//private SemaphoreSlim itemsAvaiable;
		// private SemaphoreSlim spaceAvaiable;

		private SemaphoreAsync itemsAvaiable;
		private SemaphoreAsync spaceAvaiable;

		private object mutex;
		private LinkedList<T> items;

		public BlockingQueueAsync(int maxItems) {
			itemsAvaiable = new SemaphoreAsync(0, maxItems);
			spaceAvaiable = new SemaphoreAsync(maxItems, maxItems);
			mutex = new object();
			items = new LinkedList<T>();
		}

		/// <summary>
		/// This "Put" version is synchronous
		/// Creating a "Put"  TAP version is left as an exercise
		/// </summary>
		/// <param name="item"></param>
		public void Put(T item) {
			spaceAvaiable.Acquire(1);

			lock(mutex) {
				items.AddLast(item);
			}
			itemsAvaiable.Release(1);
		}

		/// <summary>
		/// This "Take" version is synchronous
		/// </summary>
		/// <param name="item"></param>
		public T Take() {
			itemsAvaiable.Acquire(1);
			T item;
			lock (mutex) {
				item = items.First.Value;
				items.RemoveFirst();
			}
			spaceAvaiable.Release(1);
			return item;
		}

		/// <summary>
		/// A TAP version with "manual" continuation
		/// </summary>
		/// <returns></returns>
		public Task<T> TakeAsync() {
			ShowCurrentThread("Start TakeAsync");
			Task t = itemsAvaiable.AcquireAsync(1);
			return t.ContinueWith(ant =>
			{
				T item;

				ShowCurrentThread("After Wait on TakeAsync");
				lock (mutex) {
					item = items.First.Value;
					items.RemoveFirst();
				}
				spaceAvaiable.Release(1);
				return item;

			});
		}

		/// <summary>
		/// A TAP version using an async method
		/// </summary>
		/// <returns>
		/// A task representing the take operation
		/// </returns>
		public async Task<T> Take1Async() {
			ShowCurrentThread("Start Take1Async");
			await itemsAvaiable.AcquireAsync(1);

			ShowCurrentThread("After Wait on Take1Async");
			T item;
			lock (mutex) {
				item = items.First.Value;
				items.RemoveFirst();
			}
			spaceAvaiable.Release(1);
			return item;
		}

	}
}
