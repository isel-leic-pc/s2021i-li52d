using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace t2016i1 {
    public class A {
        public readonly int val;
        public A(int val) { this.val = val; }
    }

    public class B {
        public readonly string word;
        public B(string word) { this.word = word; }
    }

    public class C :  B {
        public C(string word) : base(word) { }
    }

    public class D : B{
        public D(string word) : base(word) { }
    }
}
