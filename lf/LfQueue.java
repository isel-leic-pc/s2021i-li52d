package pt.isel.pc.lf;

import java.util.concurrent.atomic.AtomicReference;

public class LfQueue<E> {
    private static class Node<E> {
        public  AtomicReference<Node<E>> next;
        public E value;

        public Node(E val) {
            this.value = val;
            this.next = new AtomicReference<>();
        }
        public Node( ) { this(null); }
    }

    AtomicReference<Node<E>> head, tail;

    public LfQueue() {
        Node<E> dummy = new Node<>();
        head = new AtomicReference<>(dummy);
        tail = new AtomicReference<>(dummy);
    }

    public void add(E elem) {
        Node<E> newNode = new Node<>(elem);
        do {
            Node<E> obsTail = tail.get();
            Node<E> obsTailNext = obsTail.next.get();
            if (obsTailNext == null) { // estado estável
                if (obsTail.next.compareAndSet(null, newNode)) {
                    tail.compareAndSet(obsTail, newNode);
                    return;
                }
            }
            else { // estado intermédio
                tail.compareAndSet(obsTail, obsTailNext);
            }
        }
        while(true);
    }


}
