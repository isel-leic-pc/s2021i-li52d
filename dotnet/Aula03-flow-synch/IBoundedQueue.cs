using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Aula_2020_10_14 {

    interface IBoundedQueue<T>
    {
        T Get();
        void Put(T e);
        int Capacity { get;  }
        int Size { get; }
    }
}
