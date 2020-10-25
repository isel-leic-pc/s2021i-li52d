package pt.isel.pc.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class NodeList<T> implements Iterable<T> {

    @Override
    public Iterator<T> iterator() {
        return new NodeListIterator<>(head);
    }

    private static class NodeListIterator<T> implements Iterator<T> {
        Node<T> head;
        Node<T> curr;

        private NodeListIterator(Node<T> head) {
            this.head = head;
            this.curr = head.next;
        }

        @Override
        public boolean hasNext() {
            return curr != head;
        }

        @Override
        public T next() {
            if (!hasNext()) throw new NoSuchElementException();
            T val = curr.value;
            curr = curr.next;
            return val;
        }
    }

    public static class Node<T> {
        private Node<T> next, previous;
        public final T value;

        public Node(T val) {
            this.value = val;
        }

        private  Node() {
            next = previous = this;
            value = null;
        }

        private void insertAfter(Node<T> before) {
            this.next = before.next;
            this.previous = before;
            before.next.previous = this;
            before.next = this;
        }

        private void insertBefore(Node<T> after) {
            this.next = after;
            this.previous = after.previous;
            after.previous.next = this;
            after.previous = this;
        }

        private void remove() {
            this.next.previous = this.previous;
            this.previous.next = this.next;
        }
    }

    private Node<T> head = new Node<>();
    private int count;

    public Node<T> addLast(T val) {
        Node<T> nn = new Node<>(val);
        nn.insertBefore(head);
        count++;
        return nn;
    }

    public Node<T> addFirst(T val) {
        Node<T> nn = new Node<>(val);
        nn.insertAfter(head);
        count++;
        return nn;
    }

    public void remove(Node<T> node) {
        if (empty())
            throw new NoSuchElementException();
        node.remove();
        count--;
    }

    public T first() {
        if (empty())
            throw new NoSuchElementException();
        return head.next.value;
    }

    public T last() {
        if (empty())
            throw new NoSuchElementException();
        return head.previous.value;
    }

    public Node<T> firstNode() {
        if (empty())
            throw new NoSuchElementException();
        return head.next;
    }

    public Node<T> lastNode() {
        if (empty())
            throw new NoSuchElementException();
        return head.previous;
    }

    public boolean empty() {
        return count ==0;
    }

    public int size() {
        return count;
    }

    public Node<T> removeFirstNode() {
        Node<T> rem = head.next;
        remove(rem);
        return rem;
    }


    public Node<T> removeLastNode() {
        Node<T> rem = head.previous;
        remove(rem);
        return rem;
    }

    public T removeFirst() {
        Node<T> node = removeFirstNode();
        return node.value;
    }

    public T removeLast() {
        Node<T> node = removeLastNode();
        return node.value;
    }

    // create a new empty list
    public void clear() {
        head = new Node<T>();
    }

}
