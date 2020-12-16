using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;

namespace Aula_2020_10_14
{ /// <summary>
  /// A try to produce a generic BoundedQueue
  /// using mutex and event has data and control flow synchronizers, respectively
  /// This try has a fundamental flaw at the points of code identified by "A" and "B"
  /// </summary>
  /// <typeparam name="T"></typeparam>
    public class BoundedQueue<T> : IBoundedQueue<T>
    {
        private LinkedList<T> elems;
        private int size; // current list size

        private int capacity; // maximum elements on list

        private Mutex mutex;

        private AutoResetEvent spaceAvailable; // signaled when there are 
        private AutoResetEvent elemsAvailable;

        public int Capacity => capacity;

        public int Size => size;


        public BoundedQueue(int capacity)
        {
            this.capacity = capacity;
            this.size = 0;
            this.elems = new LinkedList<T>();

            this.mutex = new Mutex();
            this.spaceAvailable = new AutoResetEvent(true);
            this.elemsAvailable = new AutoResetEvent(false);
        }

        private bool IsEmpty()
        {
            return size == 0;
        }

        private bool IsFull()
        {
            return size == capacity;
        }


        public T Get()
        {
            mutex.WaitOne(); // acquire mutex
            while (IsEmpty())
            { // must wait
                mutex.ReleaseMutex();
               // "A"
               // here there is a vulnerabilty window between release the mutex
               // and enter the event wait queue where anything can happen that can lead
               // to missing notifications
                elemsAvailable.WaitOne();
                mutex.WaitOne();
            }
            T first = elems.First();
            elems.RemoveFirst();
            size--;

            mutex.ReleaseMutex(); // release mutex
            spaceAvailable.Set(); //signal available space
            return first;

        }

        public void Put(T t)
        {
            mutex.WaitOne(); // acquire mutex

            while (IsFull())
            { // must wait
                mutex.ReleaseMutex();
                // "B"
                // here there is a vulnerabilty window between release the mutex
                // and enter the event wait queue where anything can happen that can lead
                // to missing notifications
                spaceAvailable.WaitOne();
                mutex.WaitOne();
            }
            elems.AddLast(t);
            size++;

            mutex.ReleaseMutex(); // release mutex

            elemsAvailable.Set(); //signal available space

        }
    }
}
