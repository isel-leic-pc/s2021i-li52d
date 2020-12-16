using System;
using System.Linq;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace Aula22_UniTests {
    using static Aula22_async_methods.generators.Generators;

    [TestClass]
    public class GeneratorTests {
      
        [TestMethod]
        public void FirstThreePairsTest() {
            foreach (int pair in FirstThreePairs())
                Console.WriteLine(pair);
        }

        [TestMethod]
        public void FirstTenAllPairsTest() {
            
            foreach (int pair in AllPairs().Take(10))
                Console.WriteLine(pair);
        }

    }
}
