using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace t2016i1 {
    using AsyncUtils;
    using static TaskExtensions;

    public class TAPExecute {

        private struct BC {
            public readonly B b;
            public readonly C c;

            public BC(B b, C c) {
                this.b = b; this.c = c;
            }
        }

        public interface TAPServices {
            Task<A> Oper1Async();
            Task<B> Oper2Async(A a);
            Task<C> Oper3Async(A a);
            Task<D> Oper4Async(B b, C c);
        }

        /**
         * A version using Task continuations.
         * 
         * Very convoluted, indeed.
         */
        public static Task<D> RunTasksAsync(TAPServices svc) {
            return svc.Oper1Async().
                ContinueWith(ant => {
                    Task<B> tb = svc.Oper2Async(ant.Result);
                    Task<C> tc = svc.Oper3Async(ant.Result);
                    return
                        Task.WhenAll(tb, tc).
                        ContinueWith(ant2 => {
                            return svc.Oper4Async(tb.Result, tc.Result);
                        }).
                        Unwrap();


                })
                .Unwrap(); 
        }

        /**
         * A version using TaskCompletionSource and task continuations
         * Not much better.
         */ 
        public static Task<D> RunTasks2Async(TAPServices svc) {
            TaskCompletionSource<D> promise = new TaskCompletionSource<D>();
            svc.Oper1Async().
                ContinueWith(ant => {
                    if (ant.IsFaulted) {
                        promise.SetException(ant.Exception);
                        return;
                    }
                    Task<B> tb = svc.Oper2Async(ant.Result);
                    Task<C> tc = svc.Oper3Async(ant.Result);
                    Task.WhenAll(tb, tc).
                    ContinueWith(ant2 => {
                        if (ant2.IsFaulted) {
                            promise.SetException(ant2.Exception);
                            return;
                        }
                        svc.Oper4Async(tb.Result, tc.Result).
                        ContinueWith(ant3 => {
                            if (ant2.IsFaulted) promise.SetException(ant.Exception);
                            else promise.SetResult(ant3.Result);
                        });
                    });
                });
            return promise.Task;
        }

        /**
         * Using ThenCompose and ThenCombine combinators.
         * Much better, but needs an auxiliary type 
         */
        public static Task<D> RunTasks3Async(TAPServices svc) {
            return 
                svc.Oper1Async().
                ThenCompose((A a) => {
                    return svc.Oper2Async(a)
                            .ThenCombine(svc.Oper3Async(a), (b, c) => new BC(b,c))
                            .ThenCompose((bc) => svc.Oper4Async(bc.b, bc.c));
                });
        }

        /**
         * Using an async C# method
         */
         public static async Task<D> RunAsync(TAPServices svc) {
            A a = await svc.Oper1Async();
            Task<B> tb = svc.Oper2Async(a);
            Task<C> tc = svc.Oper3Async(a);
            await Task.WhenAll(tb, tc);
            return await svc.Oper4Async(tb.Result, tc.Result);
        }
    }
}
