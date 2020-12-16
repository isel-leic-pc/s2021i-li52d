using System;
using System.Threading.Tasks;
using System.Threading;

namespace aula20_tasks_intro {
    class Tests {

        public static void SimpleTasksTest() {
            Console.WriteLine("main in thread {0}",
                        Thread.CurrentThread.ManagedThreadId);
            Task t0 = new Task(() => {
                Thread.Sleep(2000);
                Console.WriteLine("Action done in thread {0}",
                    Thread.CurrentThread.ManagedThreadId);
            });

            t0.Start();
            t0.Wait();
            Console.WriteLine("Done!");

        }

        public static void DelayTest() {
            Console.WriteLine("Start delay\n");
            AsyncOpers.DelayAsync(3000)
                .ContinueWith(__ => {
                    Console.WriteLine("Delay terminated!\n");
                });
        }

        public static void DelayWithCancellationTest() {
            CancellationTokenSource cts = new CancellationTokenSource();

            Console.WriteLine("Start delay\n");
            Task delayTask = AsyncOpers.DelayAsync(5000, cts.Token);

            delayTask.ContinueWith(ant => {
                    Console.WriteLine("Delay terminated!\n");
            }, TaskContinuationOptions.OnlyOnRanToCompletion);

            Thread.Sleep(2000); // Ugh!! Just for test purpose
            cts.Cancel();

            try {
                delayTask.Wait();
                Console.WriteLine("Ok!\n");
            }
            catch(AggregateException ae) {
                Console.WriteLine("Error!\n");
                ae.Flatten().Handle(e => {
                    Console.WriteLine(e);
                    return true;
                });
            }
        }

        public static void ExceptionallyTaskTest() {
            Task<int> t1 = Task<int>.Run(() => {
                Thread.Sleep(2000);
                throw new Exception("Error producing result");
                return 2;
            });

            try {
                Console.WriteLine("Task result = {0}", t1.Result);
            }
            catch (Exception e) {
                Console.WriteLine("Exception type = {0}", e.GetType().Name);
                AggregateException ae = e as AggregateException;
                foreach (Exception ie in ae.InnerExceptions) {
                    Console.WriteLine(ie.Message);
                }
            }

        }

        public static void SimpleCancellationTest() {
            CancellationTokenSource cts = new CancellationTokenSource();
            CancellationToken ct = cts.Token;

            Task t = Task.Run(() => {
                for (int i = 0; i < 10; ++i) {
                    ct.ThrowIfCancellationRequested();
                    Console.WriteLine(i);
                    Thread.Sleep(1000);
                }
            }, ct);
            Thread.Sleep(3000);
            cts.Cancel();
            try {
                t.Wait();
            }
            catch (AggregateException ae) {
                Console.WriteLine("error ocurred!: {0}(exception type: {1})",
                    ae.Message, ae.GetType().Name);
                //Console.WriteLine(e.Flatten().ToString());
                foreach (Exception e in ae.InnerExceptions)
                    Console.WriteLine("exception type: {0}, msg={1}",
                        e.GetType().Name, e.Message);

            }
            Console.WriteLine("Task status: {0}", t.Status);
        }

        public static void TestSynchronousContinuation() {
            Task t = Task.Run(() => {
                Thread.Sleep(2000);
                Console.WriteLine("Thread {0}", Thread.CurrentThread.ManagedThreadId);

            }).ContinueWith((ant) => {
                Thread.Sleep(2000);
                Console.WriteLine("Thread {0}", Thread.CurrentThread.ManagedThreadId);
            }); //, TaskContinuationOptions.ExecuteSynchronously);

            t.Wait();
        }



        public static void CopyFilesTest() {
            Task<long> t =
                FileUtils.Copy2FilesAsync3("fin1.dat", "fout1.dat", "fin2.dat", "fout2.dat");

            Console.WriteLine("Copy done with result {0}", t.Result);
        }


        public static void TestComposing0() {
            var t = Task.Factory.StartNew(() => {
                return 23;
            }).ContinueWith(ant => {
                return ant.Result + 1;
            }).ContinueWith(ant => {
                return ant.Result + 1;
            });
            Console.WriteLine("Expected result is 25, result is {0}", t.Result);
        }

        public static void TestComposing1() {
            var t =
                    AsyncOpers.RemoteIncrement(1)
                   .ContinueWith(ant => AsyncOpers.RemoteIncrement(ant.Result));

            Console.WriteLine("Expected result is 3, result is {0}", t.Result);
        }


        public static void TestComposing2() {
            Task<int> t = 
                    AsyncOpers.RemoteIncrement(1)
                   .ThenCompose<int, int>((i) => AsyncOpers.RemoteIncrement(i));

            Console.WriteLine("Expected result is 3, result is {0}", t.Result);
        }
    }
}
