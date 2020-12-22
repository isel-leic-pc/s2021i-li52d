using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;

namespace MoreAsyncTests {
   
    class Program {
        private static int Map(int val) {
            return val + 1;
        }

        private static Task<int> MapAsync(int val) {
            return Task.Run(() => {
                // if (val % 2 == 1)
                //    throw new InvalidOperationException("Bad value!");
                return val + 1;
            });
        }

        private static int Reduce(int val, int curr) {
            return val + curr;
        }

        private static Task<int> ReduceAsync(int val, int curr) {
            return Task.Run(() => {
                // if (val % 2 == 1)
                //    throw new InvalidOperationException("Bad value!");
                return val + curr;
            });
        }

        public static int MapReduce(int[] elems, int initial) {
            for (int i = 0; i < elems.Length; ++i)
                initial = Reduce(Map(elems[i]), initial);
            return initial;
        }

        public static  Task<int> MapReduceTasksAsync(int[] elems, int initial) {
            Task<int>[] mapTasks = new Task<int>[elems.Length];

            for (int i = 0; i < elems.Length; ++i)
                mapTasks[i] = MapAsync(elems[i]);

            int index = -1;
            int currReduce = initial;

            TaskCompletionSource<int> promise = new TaskCompletionSource<int>();

            void async_loop() {
                int idx = Interlocked.Increment(ref index);
                if (idx == mapTasks.Length) {
                    promise.SetResult(Volatile.Read(ref currReduce));
                    return;
                }
                mapTasks[idx].
                    ContinueWith(ant => ReduceAsync(ant.Result, Volatile.Read(ref currReduce))).
                    Unwrap().
                    ContinueWith(antr => {
                        Volatile.Write(ref currReduce, antr.Result);
                        async_loop();
                    });

            };

            async_loop();
            return promise.Task;
        }

        public static async Task<int> MapReduceAsync(int[] elems, int initial) {
            Task<int>[] mapTasks = new Task<int>[elems.Length];
 
            for (int i = 0; i < elems.Length; ++i)
                mapTasks[i] = MapAsync(elems[i]);

            for (int i = 0; i < mapTasks.Length; ++i) { 
                initial = await ReduceAsync(await mapTasks[i], initial);
            }  
            return initial;
        }

        static void Main(string[] args) {
            int[] elems = { 1, 2, 3 };


            // Use MapReduceAsync

        
            Console.WriteLine("result sync = {0}", MapReduce(elems, 0));

         
           Console.WriteLine("result async method = {0}", MapReduceAsync(elems, 0).Result);
 

            Console.WriteLine("result async tasks = {0}", MapReduceTasksAsync(elems, 0).Result);
        }
    }
}
