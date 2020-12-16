using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Aula22_async_methods.generators {
    public class Generators {
        public static IEnumerable<int> FirstThreePairs() {
            yield return 2;
            yield return 6;
            yield return 8;
        }

        public static IEnumerable<int> AllPairs() {
            int pair = 0;
            while (true) 
                 yield return pair = pair + 2;
        }
    }
}
