using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

/*
 * 
 *   public R MapReduce(T[] elems, R initial)	{ 
 *       foreach(int i = 0 ; i < elems.Length ; ++i)
 *       initial = Reduce(Map(elems[i]), initial); 
 *       return initial;
 *   }
 *
 *   O método Map é uma função sem efeitos colaterais e passível de múltiplas execuções em paralelo. O método
 *   Reduce não é associativo e tem que ser executado em série e pela ordem definida.
 *
 *   Realize uma versão assíncrona do método MapReduce seguindo o padrão TAP (Task-based Asynchronous Pattern) 
 *   usando a TPL e/ou os métodos assíncronos do C#. Assuma que tem disponível versões TAP dos métodos Map e Reduce. 
 *   Tire partido do paralelismo potencial existente, valorizando-se soluções que iniciem a sequência de operações 
 *   Reduce antes de estarem concluídas todas as operações Map.
 *
 */
namespace t2018i1 {
    public class Program {
        /*
         * A synchronous map
         */
        private static int Map(int val) {
            return val + 1;
        }

        /*
         * The reduce operation
         */
        private static int Reduce(int val, int curr) {
            return val + curr;
        }

        
        /*
         * synchronous method reduce
         */
        public static int MapReduce(int[] elems, int initial) {
            for (int i = 0; i < elems.Length; ++i)
                initial = Reduce(Map(elems[i]), initial);
            return initial;
        }

       
        
        static void Main(string[] args) {
            int[] elems = { 1, 2, 3 };

            IEnumerable<String> items =
                Enumerable.Range(1, 10)
                    .Select((i) => "a" + i);
 
            Console.WriteLine("result sync = {0}", MapReduce(elems, 0));
        }
    }

}
