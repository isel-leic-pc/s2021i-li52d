using System;
using System.Threading.Tasks;
using System.Threading;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Linq;
using t2016i1;
using static t2018i1.AsyncReducers;

namespace Dec17LectureTests {

    [TestClass]
    public class AsyncCompositionsTests {
        /*
         * TAPServices implementation for test purposes
         */
        public class TAPServicesImpl : TAPExecute.TAPServices {
            public Task<A> Oper1Async() { return Task.Run(() => new A(1));  }

            public Task<B> Oper2Async(A a) { return Task.Run(() => new B(a.val.ToString())); }

            public virtual Task<C> Oper3Async(A a) { return Task.Run(() => new C((a.val +1).ToString())); }

            public Task<D> Oper4Async(B b, C c) {
                return Task.Run(() => {
                    Thread.Sleep(2000);
                    return new D(b.word + c.word);
                });
            }
        }

       /*
        * TAPServices implementation with errors for test purposes
        */
        public class TAPServicesWithErrorsImpl : TAPServicesImpl {

            public static readonly string ErrorMsg = "Bad Oper3";

            public override Task<C> Oper3Async(A a) {
                return Task.Run(() => {
                    if (a.val == 1) throw new Exception(ErrorMsg);
                    return base.Oper3Async(a);
                });
            }   
        }

        /*
         * A general method for test all RunAsync alternatives
         */
        private void ProcessTest(Func<TAPExecute.TAPServices, Task<D>> run) {
            TAPExecute.TAPServices svc = new TAPServicesImpl();
            D res = run(svc).Result;
            Console.WriteLine(res.word);
            Assert.AreEqual("12", res.word);
        }


        [TestMethod]
        public void TapExecuteWithContinuationsTest() {
            ProcessTest(TAPExecute.RunTasksAsync);
        }

        [TestMethod]
        public void TapExecuteWithTaskCompletionSourceTest() {
            ProcessTest(TAPExecute.RunTasks3Async);
        }

        [TestMethod]
        public void TapExecuteWithCombinatorsTest() {
            ProcessTest(TAPExecute.RunTasks3Async);
        }

        [TestMethod]
        public void TapExecuteAsyncMethodTest() {
            ProcessTest(TAPExecute.RunAsync);
        }

       /*
        * A general method for test all RunAsync alternatives generating errors
        */
        private void ProcessTestWithError(Func<TAPExecute.TAPServices, Task<D>> run) {
            try {
                TAPExecute.TAPServices svc = new TAPServicesWithErrorsImpl();
                D res = TAPExecute.RunTasksAsync(svc).Result;
                Assert.Fail();
            }
            catch (AggregateException e) {
                Exception error = e.Flatten().InnerExceptions.First();
                Assert.AreEqual(TAPServicesWithErrorsImpl.ErrorMsg,
                   error.Message);
            }
        }

        
        [TestMethod]
        public void TapExecuteWithContinuationsWithErrorsTest() {
            ProcessTestWithError(TAPExecute.RunTasksAsync);
        }

        [TestMethod]
        public void TapExecuteWithTaskCompletionSourceWithErrorsTest() {
            ProcessTestWithError(TAPExecute.RunTasks2Async);
        }

        [TestMethod]
        public void TapExecuteWithCombinatorsWithErrorsTest() {
            ProcessTestWithError(TAPExecute.RunTasks3Async);
        }

        [TestMethod]
        public void TapExecuteAsyncMethodWithErrorsTest() {
            ProcessTestWithError(TAPExecute.RunAsync);
        }


        // MapReduce tests

        /*
         * An auxiliary method to test all MapReduce async alternatives
         */
        private void ProcessMapReduceAsync(Func<int[], int, Task<int>> reducer) {
            int[] elems = { 1, 2, 3 };
            int res = reducer(elems, 0).Result;
            Assert.AreEqual(9, res);
        }

        [TestMethod]
        public void MapReduceTaskAsyncTest() {
            ProcessMapReduceAsync(MapReduceTasksAsync);
        }

        [TestMethod]
        public void MapReduceAsyncTest() {
            ProcessMapReduceAsync(MapReduceAsync);
        }

        /*
       * An auxiliary method to test all MapReduce async alternatives
       */
        private void ProcessMapReduceAsyncWithErrors(Func<int[], int, Task<int>> reducer) {
            int[] elems = { 4, 5, 3 };
            try {
                int res = reducer(elems, 0).Result;
                Assert.Fail();
            }
            catch(AggregateException ae) {
                Exception e = ae.Flatten().InnerExceptions.First();
                Assert.IsInstanceOfType(e, typeof(InvalidOperationException));
            }    
        }

        [TestMethod]
        public void MapReduceTaskAsyncWithErrorsTest() {
            ProcessMapReduceAsyncWithErrors(MapReduceTasksAsync);
        }

        [TestMethod]
        public void MapReduceAsyncWithErrorsTest() {
            ProcessMapReduceAsyncWithErrors(MapReduceAsync);
        }
    }
}
