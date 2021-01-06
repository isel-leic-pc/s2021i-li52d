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
		private SemaphoreSlim itemsAvaiable;
		private SemaphoreSlim spaceAvaiable;
		
		private object mutex;
		private LinkedList<T> items;

		public BlockingQueueAsync(int maxItems) {
			itemsAvaiable = new SemaphoreSlim(0, maxItems);
			spaceAvaiable = new SemaphoreSlim(maxItems, maxItems);
			mutex = new object();
			items = new LinkedList<T>();
		}

		public void Put(T item) {
			spaceAvaiable.Wait();

			lock(mutex) {
				items.AddLast(item);
			}
			itemsAvaiable.Release();
		}

		public T Take() {
			itemsAvaiable.Wait();
			T item;
			lock (mutex) {
				item = items.First.Value;
				items.RemoveFirst();
			}
			spaceAvaiable.Release();
			return item;
		}

		public Task<T> TakeAsync() {
			ShowCurrentThread("Start TakeAsync");
			Task t = itemsAvaiable.WaitAsync();
			return t.ContinueWith(ant =>
			{
				T item;

				ShowCurrentThread("After Wait on TakeAsync");
				lock (mutex) {
					item = items.First.Value;
					items.RemoveFirst();
				}
				spaceAvaiable.Release();
				return item;

			});
		}

		public async Task<T> Take1Async() {
			ShowCurrentThread("Start Take1Async");
			await itemsAvaiable.WaitAsync();

			ShowCurrentThread("After Wait on Take1Async");
			T item;
			lock (mutex) {
				item = items.First.Value;
				items.RemoveFirst();
			}
			spaceAvaiable.Release();
			return item;
		}

	}
}
