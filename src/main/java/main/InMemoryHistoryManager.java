package main;

import tasktracker.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private Node<Task> head;
    private Node<Task> tail;
    private int size = 0;
    private Map<Long, Node> historyMap = new HashMap<>();

    private Node<Task> linkLast(Task task) {
        if (size >= 10) {
            final Node<Task> first = head;
            removeNode(first);
        }
        final Node<Task> newNode;
        final Node<Task> oldTail = tail;
        newNode = new Node<>(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        size++;
        Task t = newNode.task;
        historyMap.put(t.getId(), newNode);
        return newNode;
    }

    private List<Task> getTasks() {
        List<Task> listOfTasks = new ArrayList<>();
        Node<Task> temp = head;
        while(temp != null) {
            listOfTasks.add(temp.task);
            temp = temp.next;
        }
        return listOfTasks;
    }

    private void removeNode(Node node) {
        Node<Task> prevNode = node.prev;
        Node<Task> nextNode = node.next;
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
        if (task != null) {
            remove(task.getId());
            linkLast(task);
        }
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

    //удалить геттер и сеттер historyMap
    /*public Map<Long, Node> getHistoryMap() {
        return historyMap;
    }

    public void setHistoryMap(Map<Long, Node> historyMap) {
        this.historyMap = historyMap;
    }*/
}
