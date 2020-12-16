using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;

namespace AsyncUtils {

    public static class Combinators {

        /**
       * This combinator emulates the behaviour of thenCombine operation on Java CompletableFuture
       */
        public static Task<V> ThenCombine<T, U, V>(this Task<T> task1, Task<U> task2,
                                     Func<T, U, V> combiner) {
            TaskCompletionSource<V> promise = new TaskCompletionSource<V>();
            task1.ContinueWith(t1 => {
                task2.ContinueWith(t2 => {
                    if (t1.Status == TaskStatus.Canceled || t2.Status == TaskStatus.Canceled) {
                        promise.SetCanceled();
                    }
                    else if (t1.Exception != null || t2.Exception != null) {
                        // termination promise exceptionally
                        var elist = new List<Exception>();
                        if (t1.Exception != null) elist.Add(t1.Exception);
                        if (t2.Exception != null) elist.Add(t2.Exception);
                        AggregateException exc =
                            new AggregateException(elist);

                        promise.SetException(exc);
                    }
                    else promise.SetResult(combiner(t1.Result, t2.Result));
                }, TaskContinuationOptions.ExecuteSynchronously);
            }, TaskContinuationOptions.ExecuteSynchronously);
            return promise.Task;
        }



        /**
          * This combinator emulates the behaviour of thenCompose operation on Java CompletableFuture
          */
        public static Task<U> ThenCompose<T, U>(this Task<T> ant, Func<T, Task<U>> func) {
            TaskCompletionSource<U> tcs = new TaskCompletionSource<U>();

            ant.ContinueWith((t) => {
                switch (t.Status) {
                    case TaskStatus.RanToCompletion:
                        func(t.Result).ContinueWith(ant2 => {
                            if (ant2.IsCanceled) tcs.TrySetCanceled();
                            else if (ant2.IsFaulted) tcs.TrySetException(ant2.Exception);
                            else tcs.TrySetResult(ant2.Result);
                        });

                        break;
                    case TaskStatus.Canceled:
                        tcs.TrySetCanceled();
                        break;
                    case TaskStatus.Faulted:
                        tcs.TrySetException(t.Exception);
                        break;

                }
            }, TaskContinuationOptions.ExecuteSynchronously);
            return tcs.Task;
        }


        /// <summary>
        /// The combinator OrderByCompletion made on 12/12 lecture!
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <param name="_tasks"></param>
        /// <returns></returns>
        public static IEnumerable<Task<T>>
            OrderByCompletion<T>(this IEnumerable<Task<T>> _tasks) {

            List<Task<T>> tasks = _tasks.ToList();

            var promises = new TaskCompletionSource<T>[tasks.Count];
            var promTasks = new Task<T>[tasks.Count];

            for (int i = 0; i < promises.Length; ++i) {
                promises[i] = new TaskCompletionSource<T>();
                promTasks[i] = promises[i].Task;
            }

            int index = -1;

            foreach (var t in tasks) {
                t.ContinueWith((ant) => {
                    int idx = Interlocked.Increment(ref index);
                    switch (ant.Status) {
                        case TaskStatus.Faulted:
                            promises[idx].
                                SetException(ant.Exception);
                            break;
                        case TaskStatus.Canceled:
                            promises[idx].
                                SetCanceled();
                            break;
                        case TaskStatus.RanToCompletion:
                            promises[idx].SetResult(ant.Result);
                            break;
                    }
                });
            }
            return promTasks;

        }


    }
}
