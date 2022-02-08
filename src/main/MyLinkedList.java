package main;

import tasktracker.Task;

import java.util.ArrayList;
import java.util.List;

public class MyLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size = 0;

    public Node<T> linkLast(T task) {
        final Node<T> newNode;
        final Node<T> oldTail = tail;
        newNode = new Node<T>(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        size++;
        Task t = (Task) newNode.task;
        //historyMap.put(t.getId(), newNode);

        return newNode;
    }

    public List<Task> getTasks() {
        List<Task> listOfTasks = new ArrayList<>();
        Node<T> temp = head;
        while (temp != null) {
            listOfTasks.add((Task) temp.task);
            temp = temp.next;
        }
        return listOfTasks;
    }

    public boolean isEmpty() {
        if (head == null) {
            return true;
        } else {
            return false;
        }
    }

    public void removeNode(Node node) {
        if (size > 1) {
            Node<T> prevNode = node.prev;
            Node<T> nextNode = node.next;
            //Node<T> tail = prevNode.next;
            //prevNode = head;
            //nextNode = tail;
            node.next = null;
            node.prev = null;
            node.task = null;
            size--;
        } else {
            node = null;
        }
    }

    public int size() {
        return this.size;
    }
}
