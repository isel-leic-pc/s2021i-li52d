using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Experiments.TPL {
    class AsyncEnumerator {

        public static Task<T> Run<T>(IEnumerator<Task<T>> tseq) {
            Action<Task<T>> cont = null;
            TaskCompletionSource<T> tproxy = new TaskCompletionSource<T>();

            cont = (t) => {
                if (t!= null && t.Status == TaskStatus.Faulted)
                    tproxy.SetException(t.Exception);
                if (!tseq.MoveNext()) {
                    tproxy.TrySetResult(t.Result);
                }
                Task<T> t1 = tseq.Current;
                if (t1.Status == TaskStatus.Created)
                    t1.Start();
                t1.ContinueWith(cont);

            };
            cont(null);
            return tproxy.Task;
        }
    }
}
