using System;
using System.Threading.Tasks;
using System.Threading;

namespace aula21_tasks_intro2 {
    public class Tests {

       
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


        public static void CopyFilesTest() {
            Task<long> t =
                FileUtils.Copy2FilesAsync3("fin1.dat", "fout1.dat", "fin2.dat", "fout2.dat");

            Console.WriteLine("Copy done with result {0}", t.Result);
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
