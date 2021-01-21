using System;
using AsyncLib;
using System.Threading;
using System.Threading.Tasks;
using System.Linq;

using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Collections.Generic;

namespace AsynchronizersTests {
    
    [TestClass]
    public class SemaphoreAsyncTests {

        [TestMethod]
        public void SimpleSemaphoreAcquireTest() {
            SemaphoreAsync sem = new SemaphoreAsync(5, 5);

            bool res = sem.AcquireAsync(3, CancellationToken.None).Result;
            Assert.IsTrue(res);
            bool res1 =  sem.AcquireAsync(2, CancellationToken.None, Timeout.Infinite).Result;
            Assert.IsTrue(res1);
        }
         
        [TestMethod]
        [ExpectedException(typeof(TimeoutException))]
        public async Task TimeoutSemaphoreAcquireTest() {
            SemaphoreAsync sem = new SemaphoreAsync(1, 2);
            await sem.AcquireAsync(2, CancellationToken.None, 2000);
        }

        [TestMethod]
        [ExpectedException(typeof(TaskCanceledException))]
        public async Task CancellationOnSemaphoreAcquireTest() {
            SemaphoreAsync sem = new SemaphoreAsync(1, 2);
            CancellationTokenSource cts = new CancellationTokenSource();

            cts.CancelAfter(2000);
           
            await sem.AcquireAsync(2, cts.Token); 
        }

      

        [TestMethod]
        public async Task CancellationOnStartForMultithreadedSemaphoreAcquireTest() {
            SemaphoreAsync sem = new SemaphoreAsync(0, 10);
            CancellationTokenSource cts = new CancellationTokenSource();
            cts.Cancel();
            Task[] tasks = 
                        Enumerable.Range(1, 10)
                        .Select( (n) =>
                        {
                           Task<bool> tres = Task.Run(() =>
                           {
                               Console.WriteLine("Thread {0}", n);
                               return sem.AcquireAsync(1, cts.Token);
                            
                           });
                           
                            return tres;
                        })
                        .ToArray();

        
           
            await Assert.ThrowsExceptionAsync<TaskCanceledException>(async () => await   Task.WhenAll(tasks));
		
            foreach(var t in tasks) {
                Assert.IsTrue(t.IsCanceled);
         
			}

		}

        [TestMethod]
        [ExpectedException(typeof(AggregateException))]
        public void CancellationOnStartForSemaphoreAcquireTest() {
            SemaphoreAsync sem = new SemaphoreAsync(1, 2);
            CancellationTokenSource cts = new CancellationTokenSource();
            cts.Cancel();
            _ = sem.AcquireAsync(2, cts.Token, 5000).Result;

        }
    }
}
