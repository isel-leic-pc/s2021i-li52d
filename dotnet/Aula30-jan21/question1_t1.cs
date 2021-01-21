using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;

namespace training2021i
{
	public class question1_t1
	{

        public class Collectors
        {
            public static async Task<R> MapAsync<T,R>(T t) { return default(R);  }
            public static async Task<R> JoinAsync<R>(R r, R r2) { return default(R);  }
            

            /// <summary>
            /// Auxiliary method for maximizing paralelism using async methods
            /// </summary>
            /// <typeparam name="T"></typeparam>
            /// <typeparam name="R"></typeparam>
            /// <param name="item1"></param>
            /// <param name="item2"></param>
            /// <returns></returns>
            private static async Task<R> MapJoinPairAsync<T,R>(T item1, T item2) {
                Task<R> m1 = MapAsync<T,R>(item1);
                Task<R> m2 = MapAsync<T,R>(item2);
                return await JoinAsync<R>(await m1, await m2);
			}


            /// <summary>
            /// An asynchronous MapJoin operation with "async/await"
            /// maximizing potencial paralelism
            /// </summary>
            /// <typeparam name="T"></typeparam>
            /// <typeparam name="R"></typeparam>
            /// <param name="items"></param>
            /// <returns></returns>
            public async static Task<R[]> MapJoinAsync<T,R>(T[] items) {
              
                var tres = new Task<R>[items.Length/2];
                for (int i = 0; i < tres.Length; i++) {
                    tres[i] =  MapJoinPairAsync<T, R>(items[2*i], items[2*i+1]);
                }
                
                return await Task.WhenAll(tres);
            }

            /// <summary>
            /// An asynchronous MapJoin operation with manual continuations
            /// in order to maximize potencial paralelism
            /// </summary>
            /// <typeparam name="T"></typeparam>
            /// <typeparam name="R"></typeparam>
            /// <param name="items"></param>
            /// <returns></returns>
            public static Task<R[]> MapJoin2Async<T, R>(T[] items) {
              
                var tres = new Task<R>[items.Length / 2];
                for (int i = 0; i < tres.Length; i++) {
                    var t1 = MapAsync<T, R>(items[2 * i]);
                    var t2 = MapAsync<T, R>(items[2 * i + 1]);
                    tres[i] = Task.WhenAll(t1, t2)
                               .ContinueWith(_ => JoinAsync<R>(t1.Result, t2.Result))
                               .Unwrap();
                   
                }
                return Task.WhenAll(tres);
            }
        }

    }
}
