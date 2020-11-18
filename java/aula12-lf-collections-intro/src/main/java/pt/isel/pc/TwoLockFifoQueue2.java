package pt.isel.pc;


/**
 * This version use intrinsic locks
 */
public class TwoLockFifoQueue2<E> {
    private static class Node<E> {
        public volatile Node<E> next;
        public E value;

        public Node(E val) { this.value = val; this.next = null; }
        public Node( ) { this(null); }
    }

    // Question: must be volatile?
    Node<E> head, tail;

    public TwoLockFifoQueue2() {
        // Start with an empty queue
        head = tail = new Node<>();;
    }

    public void add(E elem) {
        synchronized(tail) {
            Node<E> newNode = new Node<>(elem);
            tail.next = newNode;
            tail = newNode;
        }
    }

    public E get(E elem) {
        synchronized(head) {
            if (head.next == null)
                return null; // fila vazia
            head = head.next;
            return head.value;
        }
    }
}
