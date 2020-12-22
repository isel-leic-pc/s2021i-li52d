using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace t2018i1 {
    public class AsyncReducers {
      /*
       * Async map alternative
       */
        private static Task<int> MapAsync(int val) {
            return Task.Run(() => {
                return val + 1;
            });
        }

      

        /*
         * Asynchronous reduce version
         */
        private static Task<int> ReduceAsync(int val, int curr) {
            return Task.Run(() => {
                if (curr > 10)
                   throw new InvalidOperationException("Bad value!");
                return val + curr;
            });
        }

        /**
         * An asynchronous  version with explicit continuations. 
         * Reduction must be made in a "recursive" way...
         */
        public static Task<int> MapReduceTasksAsync(int[] elems, int initial) {
            Task<int>[] tasks = new Task<int>[elems.Length];
            TaskCompletionSource<int> promise = new TaskCompletionSource<int>();
            for (int i = 0; i < elems.Length; ++i)
                tasks[i] = MapAsync(elems[i]);

            void agg_async_loop(int i, int agg) {
                if (i >= tasks.Length) promise.SetResult(agg);
                else {
                    ReduceAsync(tasks[i].Result, agg)
                    .ContinueWith(ant => {
                        if (ant.IsFaulted) promise.SetException(ant.Exception);
                        else agg_async_loop(i + 1, ant.Result);
                    });
                }
            }

            Task.WhenAll(tasks).
                ContinueWith(_ => {
                    agg_async_loop(0, initial);
                });
            return promise.Task;
        }


        /*
         * A version using an async method
         */
        public static async Task<int> MapReduceAsync(int[] elems, int initial) {
            Task<int>[] tasks = new Task<int>[elems.Length];
            for (int i = 0; i < elems.Length; ++i)
                tasks[i] = MapAsync(elems[i]);

            await Task.WhenAll(tasks);
            for (int i = 0; i < tasks.Length; ++i)
                initial = await ReduceAsync(tasks[i].Result, initial);
            return initial;
        }
    }
}
