using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;

namespace BlockingQueue
{
	class Utils
	{
		public static void ShowCurrentThread(string msg) {
			Console.WriteLine("{0} on thread {1}",
								msg, Thread.CurrentThread.ManagedThreadId);
		}
	}
}
