package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class InMemoryTasksManager implements TaskManager {
    private HashMap<Long, Task> tasks = new HashMap<>();
    private HashMap<Long, Subtask> subtasks = new HashMap<>();
    private HashMap<Long, Epic> epics = new HashMap<>();
    private HistoryManager historyManager = new InMemoryHistoryManager();
    protected Set<Task> sortedTasks = new TreeSet<>((t1, t2) -> {
        if (t1.getId().equals(t2.getId())) {
            return 0;
        } else if (t1.getStartTime().isAfter(t2.getStartTime()) || t1.getStartTime().isEqual(t2.getStartTime())) {
            return 1;
        } else {
            return -1;
        }
    });

    @Override
    public void save() throws ManagerSaveException {
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return sortedTasks;
    }

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
        sortedTasks.add(epic);
        System.out.println("Эпик добавлен");
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
    public Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic)
                                    throws ManagerSaveException {
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
        sortedTasks.add(subtask);
        System.out.println("Подзадача добавлена");
        return subtask;
    }

    @Override
    public Task createNewTask(String inputName, String inputDescription, long id) throws ManagerSaveException {
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
        sortedTasks.add(task);
        System.out.println("Задача добавлена");
        return task;
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
        sortedTasks.add(subtask);
        Iterator<Task> iterator = sortedTasks.iterator();
        while(iterator.hasNext()) {
            if (iterator.next().equals(epic)) {
                iterator.remove();
            }
        }
        sortedTasks.add(epic);
        System.out.println("Подзадача добавлена");
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
        sortedTasks.add(task);
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
        if (tasks.containsKey(inputId)) {
            Iterator<Task> iterator = sortedTasks.iterator();
            Task task1 = tasks.get(inputId);
            while(iterator.hasNext()) {
                if (iterator.next().equals(task)) {
                    iterator.remove();
                }
            }
            task1.setName(task.getName());
            task1.setDescription(task.getDescription());
            task1.setStartTime(task.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            task1.setDuration((int) task.getDuration().toDays());
            task1.setStatus(task.getStatus().toString());
            sortedTasks.add(task1);
            if (task.getId() != inputId) {
                tasks.remove(task.getId());
            }
        } else {
            System.out.println("Задачи с id " + inputId + " нет.");
        }
    }

    @Override
    public void updateSubtask(long inputId, Subtask subtask) throws ManagerSaveException {
        if (subtasks.containsKey(inputId)) {
            Subtask sub = subtasks.get(inputId);
            Iterator<Task> iterator = sortedTasks.iterator();
            while(iterator.hasNext()) {
                if (iterator.next().equals(sub)) {
                    iterator.remove();
                }
            }
            sub.setName(subtask.getName());
            sub.setDescription(subtask.getDescription());
            sub.setStartTime(subtask.getStartTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            sub.setDuration((int) subtask.getDuration().toDays());
            sub.setStatus(subtask.getStatus().toString());
            sortedTasks.add(sub);
            subtasks.remove(subtask.getId());
        } else {
            System.out.println("Подзадачи с id " + inputId + " нет.");
        }
    }

    @Override
    public void updateEpic(long inputId, Epic epic) throws ManagerSaveException {
        if (epics.containsKey(inputId)) {
            Epic epic1 = epics.get(inputId);
            epic1.setName(epic.getName());
            epic1.setDescription(epic.getDescription());
        } else {
            System.out.println("Эпика с id " + inputId + " нет.");
        }
    }

    @Override
    public void removeTask(long inputId) throws ManagerSaveException {
        if (tasks.containsKey(inputId)) {
            Iterator<Task> iterator = sortedTasks.iterator();
            while(iterator.hasNext()) {
                if (iterator.next().equals(tasks.get(inputId))) {
                    iterator.remove();
                }
            }
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
            Iterator<Task> iterator = sortedTasks.iterator();
            while(iterator.hasNext()) {
                if (iterator.next().equals(epics.get(inputId))) {
                    iterator.remove();
                }
            }
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
            Iterator<Task> iterator = sortedTasks.iterator();
            while(iterator.hasNext()) {
                if (iterator.next().equals(subtasks.get(inputId))) {
                    iterator.remove();
                }
            }
            Long epicId = subtasks.get(inputId).getEpic().getId();
            Epic epic = epics.get(epicId);
            ArrayList<Subtask> subtasks1 = epic.getSubtaskList();
            subtasks1.remove(subtasks.get(inputId));
            epic.setSubtaskList(subtasks1);
            if (epic.getSubtaskList().isEmpty()) {
                Iterator<Task> iterator1 = sortedTasks.iterator();
                while(iterator1.hasNext()) {
                    if (iterator1.next().equals(epic)) {
                        iterator1.remove();
                    }
                }
                historyManager.remove(epicId);
                epics.remove(epicId);
            }
            subtasks.remove(inputId);
            historyManager.remove(inputId);
            System.out.println("Подзадача удалена");
        } else {
            System.out.println("Подзадачи с таким id нет");
        }
    }

    @Override
    public void removeAllTasks(HashMap<Long, Task> tasks) throws ManagerSaveException {
        Iterator<Task> iterator = sortedTasks.iterator();
        for (Task task : tasks.values()) {
            while (iterator.hasNext()) {
                if (iterator.next().equals(task)) {
                    iterator.remove();
                    break;
                }
            }
            historyManager.remove(task.getId());
            tasks.remove(task.getId());
        }
        System.out.println("Задачи удалены");
    }

    @Override
    public void removeAllSubtasks(HashMap<Long, Subtask> subtasks) throws ManagerSaveException {
        for (Subtask subtask : subtasks.values()) {
            Iterator<Task> iterator = sortedTasks.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().equals(subtask)) {
                    iterator.remove();
                }
            }
            historyManager.remove(subtask.getId());
            historyManager.remove(subtask.getEpic().getId());
            ArrayList<Subtask> subList = subtask.getEpic().getSubtaskList();
            subList.remove(subtask);
            subtask.getEpic().setSubtaskList(subList);
            if (subtask.getEpic().getSubtaskList().isEmpty()) {
                Iterator<Task> iterator1 = sortedTasks.iterator();
                while(iterator1.hasNext()) {
                    if (iterator1.next().equals(subtask.getEpic())) {
                        iterator1.remove();
                    }
                }
                epics.remove(subtask.getEpic().getId());
            }
            subtasks.remove(subtask.getId());
        }
        System.out.println("Подзадачи удалены");
    }

    @Override
    public void removeAllEpics(HashMap<Long, Epic> epics) throws ManagerSaveException {
        for (Epic epic : epics.values()) {
            for(Subtask subtask : epic.getSubtaskList()) {
                Iterator<Task> iterator = sortedTasks.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().equals(subtask)) {
                        iterator.remove();
                    }
                }
                historyManager.remove(subtask.getId());
                subtasks.remove(subtask.getId());
            }
            historyManager.remove(epic.getId());
            Iterator<Task> iterator1 = sortedTasks.iterator();
            while (iterator1.hasNext()) {
                if (iterator1.next().equals(epic)) {
                    iterator1.remove();
                }
            }
            epics.remove(epic.getId());
        }
        System.out.println("Эпики удалены");
    }

    @Override
    public void removeAllTasks(HashMap<Long, Task> tasks, HashMap<Long, Subtask> subtasks, HashMap<Long, Epic> epics)
                               throws ManagerSaveException {
        tasks.clear();
        subtasks.clear();
        epics.clear();
        historyManager.getHistory().clear();
        sortedTasks.clear();
    }
}
