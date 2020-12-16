using System;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace Aula22_UniTests {

    using static Aula22_async_methods.AsyncMethods;

    [TestClass]
    public class AsyncMethodTests {

        [TestMethod]
        public void FirstOperAsyncTest() {
            Assert.AreEqual(3, FirstOperAsync(1).Result);
        }
    }
}
