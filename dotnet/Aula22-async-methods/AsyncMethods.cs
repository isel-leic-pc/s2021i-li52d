using System;
using System.Threading;
using System.Threading.Tasks;

namespace Aula22_async_methods {
    using static AsyncOpers;

    public class AsyncMethods {

  
        public static async Task<int> FirstOperAsync(int initial) {
            Console.WriteLine("Start FirstOperAsync in thread {0}",
                Thread.CurrentThread.ManagedThreadId);
            int res = await RemoteIncrement(initial).ConfigureAwait(true);
            Console.WriteLine("After first RemoteIncrement in FirstOperAsync in thread {0}",
                 Thread.CurrentThread.ManagedThreadId);
            var t = await RemoteIncrement(res);
            Console.WriteLine("Start Second FirstOperAsync in thread {0}",
                Thread.CurrentThread.ManagedThreadId);
            return t;
        }

        public async Task<int> SynchAsync() {
            return 2;
        }
    }
}
