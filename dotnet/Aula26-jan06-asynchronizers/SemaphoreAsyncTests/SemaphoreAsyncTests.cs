using System;
using AsyncLib;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace AsynchronizersTests {
    [TestClass]
    public class SemaphoreAsyncTests {
        [TestMethod]
        public  void SimpleSemaphoreAcquireTest() {
            SemaphoreAsync sem = new SemaphoreAsync(5, 5);

            bool res = sem.AcquireAsync(3, CancellationToken.None).Result;
            Assert.IsTrue(res);
            bool res1 =  sem.AcquireAsync(2, CancellationToken.None, Timeout.Infinite).Result;
            Assert.IsTrue(res1);

            Console.WriteLine("Done!");
        }

        [TestMethod]
        public void TimeoutSemaphoreAcquireTest() {
            SemaphoreAsync sem = new SemaphoreAsync(1, 2);

           // bool res = sem.AcquireAsync(3, CancellationToken.None, 2000).Result;
           // Assert.IsFalse(res);
            bool res1 = sem.AcquireAsync(2, CancellationToken.None, 2000).Result;
            Assert.IsFalse(res1);

            Console.WriteLine("Done!");
            Thread.Sleep(2000);
        }

        [TestMethod]
        public void CancellationOnSemaphoreAcquireTest() {
            SemaphoreAsync sem = new SemaphoreAsync(1, 2);
            CancellationTokenSource cts = new CancellationTokenSource();
            cts.CancelAfter(2000);
           
            Task<bool>  t1 = sem.AcquireAsync(2, cts.Token, 5000);
            
            try {
                Console.WriteLine("Result={0}", t1.Result);
            }
            catch(Exception e) {
                Console.WriteLine(e);
            }
            Console.WriteLine("Done!");
            
        }

        [TestMethod]
        public void CancellationOnStartForSemaphoreAcquireTest() {
            SemaphoreAsync sem = new SemaphoreAsync(1, 2);
            CancellationTokenSource cts = new CancellationTokenSource();
            cts.Cancel();

            Task<bool> t1 = sem.AcquireAsync(2, cts.Token, 5000);

            try {
                Console.WriteLine("Result={0}", t1.Result);
            }
            catch (Exception e) {
                Console.WriteLine(e);
            }
            Console.WriteLine("Done!");

        }
    }
}
