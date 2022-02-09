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

    HashMap<Long, ArrayList<Subtask>> getEpicVsSubtask();

    HashMap<Long, Long> getSubtaskVsEpic();

    Epic createNewEpic(String inputName, String inputDescription, long id);

    Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic);

    Task createNewTask(String inputName, String inputDescription, long id);

    List<Task> getHistory();

    Task getTask(long inputId);

    Subtask getSubtask(long inputId);

    Epic getEpic(long inputId);

    void updateTask(long inputId, Task task);

    void updateSubtask(long inputId, Subtask subtask);

    void updateEpic(long inputId, Epic epic);

    void removeTask(long inputId);

    void removeEpic(long inputId);

    void removeSubtask(long inputId);

    void removeAllTasks(HashMap<Long, Task> tasks, HashMap<Long, Subtask> subtasks, HashMap<Long, Epic> epics,
                        HashMap<Long, ArrayList<Subtask>> epicVsSubtask, HashMap<Long, Long> subtaskVsEpic);
}
