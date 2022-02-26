package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public interface TaskManager {

    HashMap<Long, Task> getTasks();

    HashMap<Long, Subtask> getSubtasks();

    HashMap<Long, Epic> getEpics();

    HashMap<Long, Long> getSubtaskVsEpic();

    HashMap<Long, ArrayList<Subtask>> getEpicVsSubtask();

    HistoryManager getHistoryManager();

    void setTasks(Task task);

    void setSubtasks(Subtask subtask);

    void setEpics(Epic epic);

    void setSubtaskVsEpic(Long subtaskId, Long epicId);

    void setEpicVsSubtask(Long epicId, ArrayList<Subtask> subtaskList);

    Epic createNewEpic(String inputName, String inputDescription, long id) throws ManagerSaveException;

    Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic) throws ManagerSaveException;

    Task createNewTask(String inputName, String inputDescription, long id) throws ManagerSaveException;

    List<Task> getHistory();

    Task getTask(long inputId) throws ManagerSaveException;

    Subtask getSubtask(long inputId) throws ManagerSaveException;

    Epic getEpic(long inputId) throws ManagerSaveException;

    void updateTask(long inputId, Task task) throws ManagerSaveException;

    void updateSubtask(long inputId, Subtask subtask) throws ManagerSaveException;

    void updateEpic(long inputId, Epic epic) throws ManagerSaveException;

    void removeTask(long inputId) throws ManagerSaveException;

    void removeEpic(long inputId) throws ManagerSaveException;

    void removeSubtask(long inputId) throws ManagerSaveException;

    void removeAllTasks(HashMap<Long, Task> tasks, HashMap<Long, Subtask> subtasks, HashMap<Long, Epic> epics,
                        HashMap<Long, ArrayList<Subtask>> epicVsSubtask, HashMap<Long, Long> subtaskVsEpic) throws ManagerSaveException;
}
