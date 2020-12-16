 
using System.Threading.Tasks;
using System.Threading;


namespace aula21_tasks_intro2 {
    public class AsyncOpers {


        public static Task DelayAsync(int millis) {
            TaskCompletionSource<bool> tcs = new TaskCompletionSource<bool>();

            Timer t = null;
            t = new Timer((o) => {
                t.Dispose();
                tcs.SetResult(true);
            }, null, millis, Timeout.Infinite);
            return tcs.Task;
        }


        public static Task DelayAsync(int millis, CancellationToken token) {
            TaskCompletionSource<object> tcs = new TaskCompletionSource<object>();

            Timer t = null;
            t = new Timer((o) => {
                t.Dispose();
                tcs.TrySetResult(null);
            }, null, millis, Timeout.Infinite);
            token.Register(() => {
                t.Dispose();
                tcs.TrySetCanceled();
                
            });
            return tcs.Task;
        }


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
