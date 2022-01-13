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
        long id1 = id;
        if (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
            System.out.println("Такой id уже используется");
            System.out.println("Он будет изменён автоматически");
            while (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
                id1 *= 13;
            }
        }
        Epic epic = new Epic(inputName, inputDescription, id1);
        epics.put(id1, epic);
        System.out.println("Задача добавлена");
        return epic;
    }

    public Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic) {
        long id1 = id;
        if (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
            System.out.println("Такой id уже используется");
            System.out.println("Он будет изменён автоматически");
            while (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
                id1 *= 13;
            }
            Subtask subtask = new Subtask(inputName, inputDescription, id1, epic);
            subtasks.put(id1, subtask);
            ArrayList<Subtask> subtaskList = epic.getSubtaskList();
            subtaskList.add(subtask);
            epic.setSubtaskList(subtaskList);
            epicVsSubtask.put(epic.getId(), subtaskList);
            subtaskVsEpic.put(id1, epic.getId());
            System.out.println("Задача добавлена");
            return subtask;
        }
        Subtask subtask = new Subtask(inputName, inputDescription, id1, epic);
        subtasks.put(id1, subtask);
        ArrayList<Subtask> subtaskList = epic.getSubtaskList();
        subtaskList.add(subtask);
        epic.setSubtaskList(subtaskList);
        epicVsSubtask.put(epic.getId(), subtaskList);
        subtaskVsEpic.put(id1, epic.getId());
        System.out.println("Задача добавлена");
        return subtask;
    }

    public Task createNewTask(String inputName, String inputDescription, long id) {
        long id1 = id;
        if (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
            System.out.println("Такой id уже используется");
            System.out.println("Он будет изменён автоматически");
            while (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
                id1 *= 13;
            }
        }
            Task task = new Task(inputName, inputDescription, id1);
            tasks.put(id1, task);
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

    public void updateTask(long inputId, Task task) {
        tasks.put(inputId, task);
    }

    public void updateSubtask(long inputId, Subtask subtask) {
        ArrayList<Subtask> subtasks1 = epicVsSubtask.get(subtaskVsEpic.get(inputId));
        subtasks1.remove(subtasks.get(inputId));//удаляем старую сабтаску из списка в менеджере
        subtasks1.add(subtask); //добавляем новую сабтаску в список в менеджере
        epicVsSubtask.put(subtaskVsEpic.get(inputId), subtasks1);//а новый список вносим в мапу в менеджере
        Long idEpic = subtaskVsEpic.get(inputId);
        Epic epic = epics.get(idEpic);
        ArrayList<Subtask> subtasks2 = epic.getSubtaskList();
        subtasks2.remove(subtasks.get(inputId));//удаляем старую сабтаску из списка внутри эпика
        subtasks2.add(subtask);
        epic.setSubtaskList(subtasks2);//добавляем новую сабтаску в список внутри эпика
    }

    public void updateEpic(long inputId, Epic epic) {
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
            ArrayList<Subtask> subtasks1 = epicVsSubtask.get(inputId);
            subtasks1.clear();
            Epic epic = epics.get(inputId);
            ArrayList<Subtask> subtasks2 = epic.getSubtaskList();
            subtasks2.clear();
            epic.setSubtaskList(subtasks2);
            epics.remove(inputId);
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
            subtasks.remove(inputId);
            Epic epic = epics.get(epicId);
            ArrayList<Subtask> subtasks2 = epic.getSubtaskList();
            subtasks2.remove(subtasks.get(inputId));//удаляем старую сабтаску из списка внутри эпика
            epic.setSubtaskList(subtasks2);
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
