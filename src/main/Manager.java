package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class Manager {
    private HashMap<Long, Task> tasks = new HashMap<>();
    private HashMap<Long, Subtask> subtasks = new HashMap<>();
    private HashMap<Long, Epic> epics = new HashMap<>();
    private HashMap<Long, ArrayList<Subtask>> epicVsSubtask = new HashMap<>();
    private HashMap<Long, Long> subtaskVsEpic = new HashMap<>();

    public HashMap<Long, Task> getTasks() {
        return tasks;
    }

    public HashMap<Long, Subtask> getSubtasks() {
        return subtasks;
    }

    public HashMap<Long, Epic> getEpics() {
        return epics;
    }

    public HashMap<Long, ArrayList<Subtask>> getEpicVsSubtask() {
        return epicVsSubtask;
    }

    public HashMap<Long, Long> getSubtaskVsEpic() {
        return subtaskVsEpic;
    }

    public Epic createNewEpic(String inputName, String inputDescription, long id) {
        Epic epic = new Epic(inputName, inputDescription, id);
        epics.put(id, epic);
        System.out.println("Задача добавлена");
        return epic;
    }

    public Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic) {
        Subtask subtask = new Subtask(inputName, inputDescription, id, epic);
        subtasks.put(id, subtask);
        ArrayList<Subtask> subtaskList = epic.getSubtaskList();
        subtaskList.add(subtask);
        epic.setSubtaskList(subtaskList);
        epicVsSubtask.put(epic.getId(), subtaskList);
        subtaskVsEpic.put(id, epic.getId());
        System.out.println("Задача добавлена");
        return subtask;
    }

    public Task createNewTask(String inputName, String inputDescription, long id) {
        Task task = new Task(inputName, inputDescription, id);
        tasks.put(id, task);
        System.out.println("Задача добавлена");
        return task;
    }

    public Task getTask(long inputId) {
        return tasks.get(inputId);
    }

    public Subtask getSubtask(long inputId) {
        return subtasks.get(inputId);
    }

    public Epic getEpic(long inputId) {
        return epics.get(inputId);
    }

    public void updateTask(HashMap<Long, Task> tasks, long inputId, Task task) {
        tasks.put(inputId, task);
    }

    public void updateSubtask(HashMap<Long, Subtask> subtasks, long inputId, Subtask subtask) {
        subtasks.put(inputId, subtask);
    }

    public void updateEpic(HashMap<Long, Epic> epics, long inputId, Epic epic) {
        epics.put(inputId, epic);
    }

    public void removeTask(long inputId) {
        if (tasks.containsKey(inputId)) {
            tasks.remove(inputId);
            System.out.println("Задача удалена");
        } else {
            System.out.println("Задачи с таким id нет");
        }
    }

    public void removeEpic(long inputId) {
        if (epics.containsKey(inputId)) {
            epics.remove(inputId);
            ArrayList<Subtask> subtasks1 = epicVsSubtask.get(inputId);
            subtasks1.clear();
            System.out.println("Задача удалена");
        } else {
            System.out.println("Эпика с таким id нет");
        }
    }

    public void removeSubtask(long inputId) {
        if (subtasks.containsKey(inputId)) {
            Long epicId = subtaskVsEpic.get(inputId);
            ArrayList<Subtask> subtasks1 = epicVsSubtask.get(epicId);
            subtasks1.remove(subtasks.get(inputId));
            Epic epic = epics.get(epicId);
            int count = 0;
            for (Subtask sub : subtasks1) {
                if (sub.getStatus().equals("NEW")) {
                            count++;
                }
            }
            if (count == subtasks1.size()) {
                epic.setStatus("NEW");
            }
            for (Subtask sub : subtasks1) {
                if (sub.getStatus().equals("IN_PROGRESS")) {
                    epic.setStatus("IN_PROGRESS");
                }
            }
            count = 0;
            for (Subtask sub : subtasks1) {
                if (sub.getStatus().equals("DONE")) {
                    count++;
                }
            }
            if (count == subtasks1.size()) {
                epic.setStatus("DONE");
            }
            if (subtasks1.isEmpty()) {
                epics.remove(subtaskVsEpic.get(inputId));
            }
            System.out.println("Задача удалена");
        } else {
            System.out.println("Подзадачи с таким id нет");
        }
    }

    public void removeAllTasks(HashMap<Long, Task> tasks, HashMap<Long, Subtask> subtasks, HashMap<Long, Epic> epics,
        HashMap<Long, ArrayList<Subtask>> epicVsSubtask, HashMap<Long, Long> subtaskVsEpic) {
        tasks.clear();
        subtasks.clear();
        epics.clear();
        epicVsSubtask.clear();
        subtaskVsEpic.clear();
    }
}
