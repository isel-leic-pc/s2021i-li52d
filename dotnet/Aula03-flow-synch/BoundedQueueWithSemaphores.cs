using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;

namespace Aula_2020_10_14 {
    /// <summary>
    /// a generic BoundedQueue
    /// using mutex and semaphores
    /// </summary>
    /// <typeparam name="T"></typeparam>
    public class BoundedQueueWithSemaphores<T> : IBoundedQueue<T> {
        private LinkedList<T> elems;
        private int size; // current list size

        private int capacity; // maximum elements on list

        private Mutex mutex;

        private Semaphore spaceAvailable; // signaled when there are 
        private Semaphore elemsAvailable;

        public int Capacity => capacity;

        public BoundedQueueWithSemaphores(int capacity) {
            this.capacity = capacity;
            this.size = 0;
            this.elems = new LinkedList<T>();

            this.mutex = new Mutex();
            this.spaceAvailable = new Semaphore(capacity, capacity);

            this.elemsAvailable = new Semaphore(0, capacity);
        }

        public int Size {
            get { return size; }
        }

        public T Get() {
            elemsAvailable.WaitOne();

            mutex.WaitOne(); // acquire mutex

            T first = elems.First();
            elems.RemoveFirst();
            size--;

            mutex.ReleaseMutex(); // release mutex

            spaceAvailable.Release(); //signal available space
            return first;

        }

        public void Put(T t) {

            spaceAvailable.WaitOne();

            mutex.WaitOne(); // acquire mutex

            elems.AddLast(t);
            size++;

            mutex.ReleaseMutex(); // release mutex

            elemsAvailable.Release(); //signal available space

        }
    }
}

