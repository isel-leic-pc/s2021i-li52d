using System;
using AsyncLib;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestTools.UnitTesting;

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
        [ExpectedException(typeof(AggregateException))]
        public void CancellationOnStartForSemaphoreAcquireTest() {
            SemaphoreAsync sem = new SemaphoreAsync(1, 2);
            CancellationTokenSource cts = new CancellationTokenSource();
            cts.Cancel();

            bool res = sem.AcquireAsync(2, cts.Token, 5000).Result ;

        }
    }
}
