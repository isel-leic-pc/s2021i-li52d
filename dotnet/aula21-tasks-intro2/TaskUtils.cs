using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace aula21_tasks_intro2 {
    public static class TaskUtils {

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

        /**
         * A possible implementation of a combinator that 
         * returns a task that complete when all the received tasks complete
         * 
         * The task results and eventual exceptions are collected in concurrent queues
         * The return task completes in state:
         *      Cancelled - if one or received tasks were cancelled
         *      Faulted - if one or more received tasks terminated with  an exception
         *      RanToCompletion - if all received tasks terminated normally
         */
        public static Task<V[]> WhenAll<V>(Task<V>[] tasks) {
            TaskCompletionSource<V[]> promise = new TaskCompletionSource<V[]>();
            bool cancelled = false;
            ConcurrentQueue<Exception> exceptions = new ConcurrentQueue<Exception>();
            ConcurrentQueue<V> results = new ConcurrentQueue<V>();
            int index = 0;

            foreach (var t in tasks) {
                t.ContinueWith(__ => {
                    // collect termination info
                    if (t.IsCanceled)
                        Volatile.Write(ref cancelled, true);
                    else if (t.IsFaulted) exceptions.Enqueue(t.Exception);
                    else results.Enqueue(t.Result);

                    if ((index = Interlocked.Increment(ref index)) == tasks.Length) {
                        if (Volatile.Read(ref cancelled)) promise.SetCanceled();
                        else if (!exceptions.IsEmpty) promise.SetException(exceptions);
                        else promise.SetResult(results.ToArray());
                    }
                });
            }
            return promise.Task;
        }



        public static Task<Task<V>> WhenAny<V>(Task<V>[] tasks) {
            // TODO
            return null;
        }

        public static Task<Task<V>> WhenAny<V>(List<Task<V>> tasks) {
            return WhenAny(tasks.ToArray<Task<V>>());
        }
    }

       
}

