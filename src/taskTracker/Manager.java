package taskTracker;

import java.util.ArrayList;
import java.util.HashMap;

public class Manager {
    private HashMap<Long, Task> tasks = new HashMap<>();
    private HashMap<Long, Subtask> subtasks = new HashMap<>();
    private HashMap<Long, Epic> epics = new HashMap<>();
    private HashMap<Long, ArrayList<Long>> epicVsSubtask = new HashMap<>(); //здесь информация, по которой эпик знает
                                                                            //какие сабтаски в него входят.
    private HashMap<Long, Long> subtaskVsEpic = new HashMap<>(); //здесь id сабтаски знает какой id её эпика

    public HashMap<Long, Task> getTasks() {
        return tasks;
    }

    public HashMap<Long, Subtask> getSubtasks() {
        return subtasks;
    }

    public HashMap<Long, Epic> getEpics() {
        return epics;
    }

    public HashMap<Long, ArrayList<Long>> getEpicVsSubtask() {
        return epicVsSubtask;
    }

    public void setEpicVsSubtask(HashMap<Long, ArrayList<Long>> epicVsSubtask) {
        this.epicVsSubtask = epicVsSubtask;
    }

    public HashMap<Long, Long> getSubtaskVsEpic() {
        return subtaskVsEpic;
    }

    public void setSubtaskVsEpic(HashMap<Long, Long> subtaskVsEpic) {
        this.subtaskVsEpic = subtaskVsEpic;
    }

    public Epic createNewEpic(String inputName, String inputDescription, long id) {
        Epic epic = new Epic(inputName, inputDescription, id);
        epics.put(id, epic);
        System.out.println("Задача добавлена");
        return epic;
    }

    public Subtask createNewSubtask(String inputName, String inputDescription, long id) {
        Subtask subtask = new Subtask(inputName, inputDescription, id);
        subtasks.put(id, subtask);
        System.out.println("Задача добавлена");
        return subtask;
    }

    public Task createNewTask(String inputName, String inputDescription, long id) {
        Task task = new Task(inputName, inputDescription, id);
        tasks.put(id, task);
        System.out.println("Задача добавлена");
        return task;
    }

    public void printTasks(String command, Long inputId) { //выводим в консоль списки задач
        switch (command) {
            case "2": // получение всех задач
                if (!tasks.isEmpty()) {
                    System.out.println(tasks.values());
                    System.out.println("Это все задачи");
                    break;
                } else {
                    System.out.println("Задач пока нет");
                    break;
                }
            case "3": //получение всех эпиков
                if (!epics.isEmpty()) {
                    System.out.println(epics.values());
                    System.out.println("Это все эпики");
                    break;
                } else {
                    System.out.println("Эпиков пока нет");
                    break;
                }
            case "4": //получение подзадач по id эпика
                    System.out.println(epics.get(inputId));
                    ArrayList<Long> subtaskId = epicVsSubtask.get(inputId);
                    for (Long id : subtaskId) {
                        try {
                            if (subtasks.get(id)!= null) {
                                System.out.println(subtasks.get(id)); //вроде работает как надо
                            }
                        } catch (NullPointerException e) {
                        }
                    }
                    System.out.println("Это все подзадачи эпика");
                    break;
            default:
                System.out.println("--------------------------------------");
        }
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

    public void removeSomeTask(long inputId) { //удаляем любую задачу по id
        if (tasks.containsKey(inputId)) {
            tasks.remove(inputId);
        } else if (epics.containsKey(inputId)) {
            epics.remove(inputId);
            ArrayList<Long> subtasks1 = epicVsSubtask.get(inputId);
            for (Long id : subtasks1) {
                subtasks.remove(id);
            }
        } else if (subtasks.containsKey(inputId)) {
            subtasks.remove(inputId);
            Long epicId = subtaskVsEpic.get(inputId);
            ArrayList<Long> subtasks1 = epicVsSubtask.get(epicId);
            subtasks1.remove(inputId);
            Epic epic = epics.get(epicId);
            int count = 0;
            for (Subtask sub : subtasks.values()) {
                if (sub.getStatus().equals("NEW")) {
                    for (Long newId : subtasks1) {
                        if (sub.getId().equals(newId)) {
                            count++;
                        }
                    }
                }
            }
            if (count == subtasks1.size()) {
                epic.setStatus("NEW");
            }
            for (Subtask sub : subtasks.values()) {
                if (sub.getStatus().equals("IN_PROGRESS")) {
                    for (Long newId : subtasks1) {
                        if (sub.getId().equals(newId)) {
                            epic.setStatus("IN_PROGRESS");
                        }
                    }
                }
            }
            count = 0;
            for (Subtask sub : subtasks.values()) {
                if (sub.getStatus().equals("DONE")) {
                    for (Long newId : subtasks1) {
                        if (sub.getId().equals(newId)) {
                            count++;
                        }
                    }
                }
            }
            if (count == subtasks1.size()) {
                epic.setStatus("DONE");
            }
            if (subtasks1.isEmpty()) {
                epics.remove(subtaskVsEpic.get(inputId));
            }
        } else {
            System.out.println("Задачи под таким id нет");
        }
    }

    public void removeAllTasks(HashMap<Long, Task> tasks, HashMap<Long, Subtask> subtasks, HashMap<Long, Epic> epics,
        HashMap<Long, ArrayList<Long>> epicVsSubtask, HashMap<Long, Long> subtaskVsEpic) {
        tasks.clear();
        subtasks.clear();
        epics.clear();
        epicVsSubtask.clear();
        subtaskVsEpic.clear();
    }
}
