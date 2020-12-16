using System;
using System.Threading.Tasks;
using System.Threading;


namespace Aula22_async_methods {
    public class AsyncOpers {

        // This method represents a remote API.
        public static Task<int> RemoteIncrement(int n) {
            return Task<int>.Factory.StartNew(
                (obj) => {
                    // Simulate a slow operation
                    Thread.Sleep(2000);
                  
                    int x = (int)obj;

                    return ++x;
                },
                n);
        }

 
    }
}
