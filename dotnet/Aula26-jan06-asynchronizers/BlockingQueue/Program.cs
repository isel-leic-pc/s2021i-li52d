using AsyncLib;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace BlockingQueue
{
	using static Utils;

	class Program
	{
		/// <summary>
		/// A simple example of BlockingQueueAsync use
		/// </summary>
		/// <param name="args"></param>
		/// <returns></returns>
		static async Task Main(string[] args) {
			BlockingQueueAsync<int> queue = new BlockingQueueAsync<int>(10);

			ShowCurrentThread("Start Main");
			Task<int> taskInt = queue.TakeAsync();

			Task t = Task.Run(() =>
			{
				Task.Delay(5000)
				.ContinueWith(_ => queue.Put(23));

			});

			ShowCurrentThread("All Started");
			Console.WriteLine(await taskInt);
			ShowCurrentThread("All Ended");
				 
		}
	}
}
