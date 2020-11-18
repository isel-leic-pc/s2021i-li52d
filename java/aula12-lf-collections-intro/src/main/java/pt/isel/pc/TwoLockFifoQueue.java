package pt.isel.pc;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TwoLockFifoQueue<E> {
    private static class Node<E> {
        public  volatile Node<E> next;
        public E value;

        public Node(E val) {
            this.value = val;
            this.next = null;
        }
        public Node( ) { this(null); }
    }

    Lock lockAdd, lockRemove;
    Node<E> head, tail;

    public TwoLockFifoQueue() {
        lockAdd = new ReentrantLock();
        lockRemove = new ReentrantLock();
        head = tail = new Node<>();;
    }

    public void add(E elem) {
        lockAdd.lock();
        try {
            Node<E> newNode = new Node<>(elem);
            tail.next = newNode;
            tail = newNode;
        }
        finally {
            lockAdd.unlock();
        }
    }

    public E get(E elem) {
       lockRemove.lock();
       try {
           if (head.next == null)
               return null; // fila vazia
           head = head.next;
           return head.value;
       }
       finally {
           lockRemove.unlock();
       }
    }
}
