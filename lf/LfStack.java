package pt.isel.pc.lf;

import pt.isel.pc.TwoLockFifoQueue;

import java.util.concurrent.atomic.AtomicReference;

public class LfStack<E> {
    private static class Node<E> {
        public  volatile Node<E> next;
        public E value;

        public Node(E val) {
            this.value = val;
            this.next = null;
        }
        public Node( ) { this(null); }
    }

    private volatile AtomicReference<Node<E>> head =
            new AtomicReference<>();

    public void push(E elem) {
        Node<E> newNode = new Node<>(elem);
        do {
            Node<E> obsHead = head.get();
            newNode.next = obsHead;
            if (head.compareAndSet(obsHead, newNode))
                return;
        }
        while(true);
    }

    public E pop() {
        do {
            Node<E> obsHead = head.get();
            if (obsHead == null) return null;
            if (head.compareAndSet(obsHead, obsHead.next))
                return obsHead.value;
        }
        while(true);
    }
}
