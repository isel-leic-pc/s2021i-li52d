package pt.isel.pc.lf;

import java.util.concurrent.atomic.AtomicReference;

public class LFQueue<E> {
    private static class Node<E> {
        public  AtomicReference<Node<E>> next;
        public E value;

        public Node(E val) {
            this.value = val;
            this.next = new AtomicReference<>();
        }
        public Node( ) { this(null); }
    }

    private AtomicReference<Node<E>> head, tail;

    public LFQueue() {
        Node<E> dummy = new Node<>();
        head = new AtomicReference<>(dummy);
        tail = new AtomicReference<>(dummy);
    }

    public void push(E elem) {
        Node<E> newNode = new Node<>(elem);

        do {
           Node<E> obsTail = tail.get();
           Node<E> obsTailNext = obsTail.next.get();

           if (obsTailNext != null)
               tail.compareAndSet(obsTail, obsTailNext);
           else {
               if (obsTail.next.compareAndSet(null, newNode)) {
                   tail.compareAndSet(obsTail, newNode);
                   return;
               }
           }
        }
        while(true);
    }
}
