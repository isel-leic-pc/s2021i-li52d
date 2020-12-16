using System;
using System.Threading;
using System.Threading.Tasks;

namespace Aula22_async_methods {
    using static AsyncOpers;

    public class AsyncMethods {


        public static async Task<int> FirstOperAsync(int initial) {
            Console.WriteLine("FirstOperAsync Start in thread {0}", Thread.CurrentThread.ManagedThreadId);
            int res = await RemoteIncrement(initial);
            Console.WriteLine("FirstOperAsync First Done in thread {0}", Thread.CurrentThread.ManagedThreadId);
            var t = await RemoteIncrement(res);
            Console.WriteLine("FirstOperAsync  Done in thread {0}", Thread.CurrentThread.ManagedThreadId);
            return t;
        }

    }
}
