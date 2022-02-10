package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        Task task1 = taskManager.createNewTask("qwe", "ewq", 1);
        Task task2 = taskManager.createNewTask("rty", "ytr", 2);
        Epic epic1 = taskManager.createNewEpic("asd", "dsa", 4);
        Epic epic2 = taskManager.createNewEpic("ghj", "jhg", 5);
        Subtask subtask1 = taskManager.createNewSubtask("tyu", "uyt", 6, epic2);
        Subtask subtask2 = taskManager.createNewSubtask("uio", "oiu", 7, epic2);
        Subtask subtask3 = taskManager.createNewSubtask("bnm", "mnb", 8, epic2);

        taskManager.getTask(1);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getTask(2);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getTask(2);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getTask(1);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getEpic(4);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getEpic(5);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getSubtask(6);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getSubtask(7);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getSubtask(8);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getSubtask(7);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getEpic(5);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
        taskManager.getEpic(4);
        for (Task t : taskManager.getHistory()) {
            System.out.println(t);
        }
        System.out.println("Следующий");
    }
}
