package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.util.*;

public class InMemoryTasksManager implements TaskManager {
    private HashMap<Long, Task> tasks = new HashMap<>();
    private HashMap<Long, Subtask> subtasks = new HashMap<>();
    private HashMap<Long, Epic> epics = new HashMap<>();
    private HashMap<Long, ArrayList<Subtask>> epicVsSubtask = new HashMap<>();
    private HashMap<Long, Long> subtaskVsEpic = new HashMap<>();
    private HistoryManager historyManager = new InMemoryHistoryManager();

    @Override
    public void setStartTime(String startTime, long id) throws ManagerSaveException {
        if (tasks.containsKey(id)) {
            Task task = tasks.get(id);
            task.setStartTime(startTime);
        } else if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            subtask.setStartTime(startTime);
        }
    }

    @Override
    public void setDuration(int duration, long id) throws ManagerSaveException {
        if (tasks.containsKey(id)) {
            Task task = tasks.get(id);
            task.setDuration(duration);
        } else if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            subtask.setDuration(duration);
        }
    }

    @Override
    public void setStatus(String status, long id) throws ManagerSaveException {
        if (tasks.containsKey(id)) {
            Task task = tasks.get(id);
            task.setStatus(status);
        } else if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            subtask.setStatus(status);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

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
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public Epic createNewEpic(String inputName, String inputDescription, long id) throws ManagerSaveException {
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
    public void setTasks(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void setSubtasks(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
    }

    @Override
    public void setEpics(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void setEpicVsSubtask(Long epicId, ArrayList<Subtask> subtaskList) {
        epicVsSubtask.put(epicId, subtaskList);
    }

    @Override
    public void setSubtaskVsEpic(Long subtaskId, Long epicId) {
        subtaskVsEpic.put(subtaskId, epicId);
    }

    @Override
    public Subtask createNewSubtask(String inputName, String inputDescription, long id, String startTime,
                                    int duration, Epic epic) throws ManagerSaveException {
        long id1 = id;
        if (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
            System.out.println("Такой id уже используется");
            System.out.println("Он будет изменён автоматически");
            while (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
                id1 *= 13;
            }
        }
        Subtask subtask = new Subtask(inputName, inputDescription, id1, startTime, duration, epic);
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
    public Task createNewTask(String inputName, String inputDescription, long id, String startTime,
                              int duration) throws ManagerSaveException {
        long id1 = id;

        if (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
            System.out.println("Такой id уже используется");
            System.out.println("Он будет изменён автоматически");
            while (tasks.containsKey(id1) || subtasks.containsKey(id1) || epics.containsKey(id1)) {
                id1 *= 13;
            }
        }
            Task task = new Task(inputName, inputDescription, id1, startTime, duration);
            tasks.put(id1, task);
            System.out.println("Задача добавлена");
            return task;
    }

    @Override
    public Task getTask(long inputId) throws ManagerSaveException {
        historyManager.add(tasks.get(inputId));
        return tasks.get(inputId);
    }

    @Override
    public Subtask getSubtask(long inputId) throws ManagerSaveException {
        historyManager.add(subtasks.get(inputId));
        return subtasks.get(inputId);
    }

    @Override
    public Epic getEpic(long inputId) throws ManagerSaveException {
        historyManager.add(epics.get(inputId));
        return epics.get(inputId);
    }

    @Override
    public void updateTask(long inputId, Task task) throws ManagerSaveException {
        tasks.put(inputId, task);
    }

    @Override
    public void updateSubtask(long inputId, Subtask subtask) throws ManagerSaveException {
        ArrayList<Subtask> subtasks1 = epicVsSubtask.get(subtaskVsEpic.get(inputId));
        subtasks1.remove(subtasks.get(inputId));//удаляем старую сабтаску из списка в менеджере
        subtasks1.add(subtask); //добавляем новую сабтаску в список в менеджере
        epicVsSubtask.put(subtaskVsEpic.get(inputId), subtasks1);//а новый список вносим в мапу в менеджере
        Long idEpic = subtaskVsEpic.get(inputId);
        Epic epic = epics.get(idEpic);
        ArrayList<Subtask> subtasks2 = epic.getSubtaskList();
        subtasks2.remove(subtasks.get(inputId));//удаляем старую сабтаску из списка внутри эпика
        subtasks2.add(subtask);
        subtasks.put(inputId, subtask);
        epic.setSubtaskList(subtasks2);//добавляем новую сабтаску в список внутри эпика
    }

    @Override
    public void updateEpic(long inputId, Epic epic) throws ManagerSaveException {
        epics.put(inputId, epic);
    }

    @Override
    public void removeTask(long inputId) throws ManagerSaveException {
        if (tasks.containsKey(inputId)) {
            tasks.remove(inputId);
            historyManager.remove(inputId);
            System.out.println("Задача удалена");
        } else {
            System.out.println("Задачи с таким id нет");
        }
    }

    @Override
    public void removeEpic(long inputId) throws ManagerSaveException {
        if (epics.containsKey(inputId)) {
            Epic epic = epics.get(inputId);
            ArrayList<Subtask> subtasks1 = epic.getSubtaskList();
            for (Subtask sub : subtasks1) {
                historyManager.remove(sub.getId());
                subtasks.remove(sub.getId());
            }
            subtasks1.clear();
            epic.setSubtaskList(subtasks1);
            epics.remove(inputId);
            historyManager.remove(inputId);
            System.out.println("Задача удалена");
        } else {
            System.out.println("Эпика с таким id нет");
        }
    }

    @Override
    public void removeSubtask(long inputId) throws ManagerSaveException {
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
            historyManager.remove(inputId);
            System.out.println("Задача удалена");
        } else {
            System.out.println("Подзадачи с таким id нет");
        }
    }

    @Override
    public void removeAllTasks(HashMap<Long, Task> tasks, HashMap<Long, Subtask> subtasks, HashMap<Long, Epic> epics,
        HashMap<Long, ArrayList<Subtask>> epicVsSubtask, HashMap<Long, Long> subtaskVsEpic) throws ManagerSaveException {
        tasks.clear();
        subtasks.clear();
        epics.clear();
        epicVsSubtask.clear();
        subtaskVsEpic.clear();
    }
}
