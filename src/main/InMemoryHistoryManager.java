package main;

import tasktracker.Task;

import java.util.*;


public class InMemoryHistoryManager<T> implements HistoryManager{

    Map<Long, Node> historyMap = new HashMap<>();

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

    @Override
    public void add(Task task) {
        if (historyMap.containsKey(task.getId())) {
            remove(task.getId());
        }
        if (isEmpty()) {
            linkLast((T) task);
            List<Task> l = getTasks();
        } else {
            linkLast((T) task);
            List<Task> list = getTasks();
            if (list.size() >= 10) {
                getTasks().remove(0);
            }
        }
    }

    @Override
    public void remove(Long id) {
        getTasks().remove(id);
        removeNode(historyMap.get(id));
        historyMap.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }
}
