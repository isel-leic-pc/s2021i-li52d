using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SynchUtils {
    public sealed class BatchReqQueue<T> {

        public sealed class Request {
           public  T value;
            public Request(T value) {
                this.value = value;
            }
        }

        private int count;

        private Request current;

        public BatchReqQueue(T r) {
            current = new Request(r);
            count = 0;
        }

        public Request Add() {
            count++;
            return current;
        }

        public void Remove(Request r) {
            if (count == 0 || r != current)
                throw new InvalidOperationException();
            count--;   
        }

        public void NewBatch(T t) {
            current = new Request(t);
            count = 0;
        }

        public int Count => count;

        public Request Current => current;
    }
}
