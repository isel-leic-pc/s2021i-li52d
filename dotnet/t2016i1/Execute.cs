using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace t2016i1 {
    public class Execute {
       


        public interface Services {

            A Oper1();
            B Oper2(A a);
            C Oper3(A a);
            D Oper4(B b, C c);
        }

        public static D Run(Services svc) {
            var a = svc.Oper1();
            return svc.Oper4(svc.Oper2(a), svc.Oper3(a));
        }
    }
}
