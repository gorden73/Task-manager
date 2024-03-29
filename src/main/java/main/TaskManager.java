package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.util.*;

public interface TaskManager {

    void save() throws ManagerSaveException;

    Set<Task> getPrioritizedTasks();

    void setStartTime(String startTime, long id) throws ManagerSaveException;

    void setDuration(int duration, long id) throws ManagerSaveException;

    void setStatus(String status, long id) throws ManagerSaveException;

    HashMap<Long, Task> getTasks();

    HashMap<Long, Subtask> getSubtasks();

    HashMap<Long, Epic> getEpics();

    HistoryManager getHistoryManager();

    void setTasks(Task task);

    void setSubtasks(Subtask subtask);

    void setEpics(Epic epic);

    Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic) throws ManagerSaveException;

    Task createNewTask(String inputName, String inputDescription, long id) throws ManagerSaveException;

    Epic createNewEpic(String inputName, String inputDescription, long id) throws ManagerSaveException;

    Subtask createNewSubtask(String inputName, String inputDescription, long id, String startTime, int duration,
                             Epic epic) throws ManagerSaveException;

    Task createNewTask(String inputName, String inputDescription, long id, String startTime, int duration)
                       throws ManagerSaveException;

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

    void removeAllTasks(HashMap<Long, Task> tasks) throws ManagerSaveException;

    void removeAllSubtasks(HashMap<Long, Subtask> subtasks) throws ManagerSaveException;

    void removeAllTasks(HashMap<Long, Task> tasks, HashMap<Long, Subtask> subtasks, HashMap<Long, Epic> epics)
                        throws ManagerSaveException;
}
