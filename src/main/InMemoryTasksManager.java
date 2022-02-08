package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTasksManager implements TaskManager {
    private final HashMap<Long, Task> tasks = new HashMap<>();
    private final HashMap<Long, Subtask> subtasks = new HashMap<>();
    private final HashMap<Long, Epic> epics = new HashMap<>();
    private final HashMap<Long, ArrayList<Subtask>> epicVsSubtask = new HashMap<>();
    private final HashMap<Long, Long> subtaskVsEpic = new HashMap<>();

    HistoryManager historyManager = new InMemoryHistoryManager();

    @Override
    public HashMap<Long, Task> getTasks() {
        return tasks;
    }

    @Override
    public HashMap<Long, Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public HashMap<Long, Epic> getEpics() {
        return epics;
    }

    @Override
    public HashMap<Long, ArrayList<Subtask>> getEpicVsSubtask() {
        return epicVsSubtask;
    }

    @Override
    public HashMap<Long, Long> getSubtaskVsEpic() {
        return subtaskVsEpic;
    }

    @Override
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

    @Override
    public Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic) {
        long id1 = id;
        if (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
            System.out.println("Такой id уже используется");
            System.out.println("Он будет изменён автоматически");
            while (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
                id1 *= 13;
            }
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

    @Override
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

    @Override
    public Task getTask(long inputId) {
        historyManager.add(tasks.get(inputId));
        return tasks.get(inputId);
    }

    @Override
    public Subtask getSubtask(long inputId) {
        historyManager.add(subtasks.get(inputId));
        return subtasks.get(inputId);
    }

    @Override
    public Epic getEpic(long inputId) {
        historyManager.add(epics.get(inputId));
        return epics.get(inputId);
    }

    @Override
    public void updateTask(long inputId, Task task) {
        tasks.put(inputId, task);
    }

    @Override
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

    @Override
    public void updateEpic(long inputId, Epic epic) {
        epics.put(inputId, epic);
    }

    @Override
    public void removeTask(long inputId) {
        if (tasks.containsKey(inputId)) {
            tasks.remove(inputId);
            System.out.println("Задача удалена");
        } else {
            System.out.println("Задачи с таким id нет");
        }
    }

    @Override
    public void removeEpic(long inputId) {
        if (epics.containsKey(inputId)) {
            Epic epic = epics.get(inputId);
            ArrayList<Subtask> subtasks1 = epic.getSubtaskList();
            for (Subtask sub : subtasks1) {
                subtasks.remove(sub.getId()); //удаляем сабтаски из мапы, чтобы после удаления эпика нельзя
            }                                 // было получить по id сабтаску удаленного эпика
            subtasks1.clear();
            epic.setSubtaskList(subtasks1);
            epics.remove(inputId);
            System.out.println("Задача удалена");
        } else {
            System.out.println("Эпика с таким id нет");
        }
    }

    @Override
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

    @Override
    public void removeAllTasks(HashMap<Long, Task> tasks, HashMap<Long, Subtask> subtasks, HashMap<Long, Epic> epics,
        HashMap<Long, ArrayList<Subtask>> epicVsSubtask, HashMap<Long, Long> subtaskVsEpic) {
        tasks.clear();
        subtasks.clear();
        epics.clear();
        epicVsSubtask.clear();
        subtaskVsEpic.clear();
    }
}
