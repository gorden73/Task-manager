package main;

import tasktracker.Task;

import java.util.*;

public class InMemoryHistoryManager<T> implements HistoryManager {
    private Node<T> head;
    private Node<T> tail;
    private int size = 0;
    Map<Long, Node> historyMap = new HashMap<>();

    public Node<T> linkLast(T task) {
        if (size >= 10) {
            final Node<T> first = head;
            removeNode(first);
        }
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
        historyMap.put(t.getId(), newNode);
        return newNode;
    }

    public List<Task> getTasks() {
        List<Task> listOfTasks = new ArrayList<>();
        Node<T> temp = head;
        while(temp != null) {
            listOfTasks.add((Task) temp.task);
            temp = temp.next;
        }
        return listOfTasks;
    }

    public void removeNode(Node node) {
        Node<T> prevNode = node.prev;
        Node<T> nextNode = node.next;
        if (size == 1) {
            head = null;
            tail = null;
            node.task = null;
        } else if (size > 1) {
            if (prevNode == null) {
                head = nextNode;
                nextNode.prev = null;
                node.next = null;
                node.task = null;
            } else if (nextNode == null) {
                tail = prevNode;
                prevNode.next = null;
                node.prev = null;
                node.task = null;
            } else {
                prevNode.next = nextNode;
                nextNode.prev = prevNode;
                node.next = null;
                node.prev = null;
                node.task = null;
            }
        }
        if (size != 0) {
            size--;
        }
    }

    @Override
    public void add(Task task) {
        if (historyMap.containsKey(task.getId())) {
            remove(task.getId());
        }
        linkLast((T) task);
    }

    @Override
    public void remove(Long id) {
        if (historyMap.containsKey(id)) {
            getTasks().remove(id);
            removeNode(historyMap.get(id));
            historyMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
