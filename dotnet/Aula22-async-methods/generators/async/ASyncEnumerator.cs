using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Aula22_async_methods.generators.async {
    public static class AsyncEnumerator {

        public static Task<T> Run<T>(this IEnumerator<Task<T>> tseq) {

            TaskCompletionSource<T> tproxy = new TaskCompletionSource<T>();

            void cont(Task<T> t) {
                if (t != null && t.Status == TaskStatus.Faulted)
                    tproxy.TrySetException(t.Exception);
                else if (!tseq.MoveNext()) {
                    tproxy.TrySetResult(t.Result);
                }
                else {
                    Task<T> t1 = tseq.Current;
                    if (t1.Status == TaskStatus.Created)
                        t1.Start();
                    t1.ContinueWith(cont);
                }

            };

            cont(null);
            return tproxy.Task;
        }


        public static Task Run(this IEnumerator<Task> tseq) {

            TaskCompletionSource<bool> tproxy = new TaskCompletionSource<bool>();

            void cont(Task t) {
                if (t != null && t.Status == TaskStatus.Faulted)
                    tproxy.TrySetException(t.Exception);
                else if (!tseq.MoveNext()) {
                    tproxy.TrySetResult(true);
                }
                else {
                    Task t1 = tseq.Current;
                    if (t1.Status == TaskStatus.Created)
                        t1.Start();
                    t1.ContinueWith(cont);
                }

            };

            cont(null);
            return tproxy.Task;
        }


        public static Task<T> Run<T>(this IEnumerable<Task<T>> tseq) {
            return Run(tseq.GetEnumerator());
        }

        public static Task Run(this IEnumerable<Task> tseq) {
            return  Run(tseq.GetEnumerator());
        }
    }
}
