package main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest extends TaskManagerTest {
    File fileToSave;

    @Test
    public void saveWhenFileIsEmpty() throws ManagerSaveException, IOException {
        taskManager = FileBackedTasksManager.loadFromFile(fileToSave);
        assertEquals(0, taskManager.getTasks().size());
        assertEquals(0, taskManager.getSubtasks().size());
        assertEquals(0, taskManager.getEpics().size());
        assertEquals(0, taskManager.getHistory().size());
        assertEquals(0, taskManager.getPrioritizedTasks().size());
        taskManager.save();
        assertTrue(fileToSave.isFile());
    }

    @Test
    public void saveWhenEpicsWithoutSubtasks() throws ManagerSaveException, IOException {
        Task task1 = new Task("a", "b", 1, "09.08.2002", 3);
        taskManager.getTasks().put(1L, task1);
        taskManager.getPrioritizedTasks().add(task1);
        taskManager.getHistoryManager().add(taskManager.getTasks().get(1L));
        Epic epic1 = new Epic("a", "b", 2);
        taskManager.getEpics().put(2L, epic1);
        taskManager.getPrioritizedTasks().add(epic1);
        taskManager.getHistoryManager().add(taskManager.getEpics().get(2L));
        taskManager.save();
        taskManager = FileBackedTasksManager.loadFromFile(fileToSave);
        assertEquals(1, taskManager.getTasks().size());
        assertEquals(0, taskManager.getSubtasks().size());
        assertEquals(1, taskManager.getEpics().size());
        assertEquals(2, taskManager.getHistory().size());
        assertEquals(2, taskManager.getPrioritizedTasks().size());
    }

    @Test
    public void saveWhenHistoryIsEmpty() throws ManagerSaveException, IOException {
        Task task1 = new Task("a", "b", 1, "09.08.2002", 3);
        taskManager.getTasks().put(1L, task1);
        taskManager.getPrioritizedTasks().add(task1);
        Epic epic1 = new Epic("a", "b", 2);
        taskManager.getEpics().put(2L, epic1);
        taskManager.getPrioritizedTasks().add(epic1);
        Subtask subtask1 = new Subtask("a", "b", 3, "05.07.2005",3, epic1);
        taskManager.getSubtasks().put(3L, subtask1);
        ArrayList<Subtask> subtaskList = epic1.getSubtaskList();
        subtaskList.add(subtask1);
        epic1.setSubtaskList(subtaskList);
        taskManager.getPrioritizedTasks().add(subtask1);
        taskManager.save();
        taskManager = FileBackedTasksManager.loadFromFile(fileToSave);
        assertEquals(1, taskManager.getTasks().size());
        assertEquals(1, taskManager.getSubtasks().size());
        assertEquals(1, taskManager.getEpics().size());
        assertEquals(0, taskManager.getHistory().size());
        assertEquals(task1, taskManager.getTask(1));
        assertEquals(subtask1, taskManager.getSubtask(3));
        assertEquals(epic1, taskManager.getEpic(2));
    }

    @Test
    public void saveDefault() throws ManagerSaveException, IOException {
        Task task1 = new Task("a", "b", 1, "09.08.2002", 3);
        taskManager.getTasks().put(1L, task1);
        taskManager.getPrioritizedTasks().add(task1);
        taskManager.getHistoryManager().add(taskManager.getTasks().get(1L));
        Epic epic1 = new Epic("a", "b", 2);
        taskManager.getEpics().put(2L, epic1);
        taskManager.getPrioritizedTasks().add(epic1);
        taskManager.getHistoryManager().add(taskManager.getEpics().get(2L));
        Subtask subtask1 = new Subtask("a", "b", 3, "05.07.2005",3, epic1);
        taskManager.getSubtasks().put(3L, subtask1);
        ArrayList<Subtask> subtaskList = epic1.getSubtaskList();
        subtaskList.add(subtask1);
        epic1.setSubtaskList(subtaskList);
        taskManager.getPrioritizedTasks().add(subtask1);
        taskManager.getHistoryManager().add(taskManager.getSubtasks().get(3L));
        assertEquals(1, taskManager.getTasks().size());
        assertEquals(1, taskManager.getSubtasks().size());
        assertEquals(1, taskManager.getEpics().size());
        assertEquals(3, taskManager.getHistory().size());
        assertEquals(task1, taskManager.getTask(1));
        assertEquals(subtask1, taskManager.getSubtask(3));
        assertEquals(epic1, taskManager.getEpic(2));
        assertEquals(task1, taskManager.getHistory().get(0));
        assertEquals(subtask1, taskManager.getHistory().get(1));
        assertEquals(epic1, taskManager.getHistory().get(2));
        taskManager.save();
        taskManager = FileBackedTasksManager.loadFromFile(fileToSave);
        assertEquals(1, taskManager.getTasks().size());
        assertEquals(1, taskManager.getSubtasks().size());
        assertEquals(1, taskManager.getEpics().size());
        assertEquals(3, taskManager.getHistory().size());
        assertEquals(task1, taskManager.getTask(1));
        assertEquals(subtask1, taskManager.getSubtask(3));
        assertEquals(epic1, taskManager.getEpic(2));
        assertEquals(task1, taskManager.getHistory().get(0));
        assertEquals(subtask1, taskManager.getHistory().get(1));
        assertEquals(epic1, taskManager.getHistory().get(2));
    }

    @Test
    public void loadFromFileWithoutHistory() throws ManagerSaveException, IOException {
        Task task1 = new Task("a", "b", 1, "09.08.2002", 3);
        taskManager.getTasks().put(1L, task1);
        taskManager.getPrioritizedTasks().add(task1);
        Epic epic1 = new Epic("a", "b", 2);
        taskManager.getEpics().put(2L, epic1);
        taskManager.getPrioritizedTasks().add(epic1);
        Subtask subtask1 = new Subtask("a", "b", 3, "05.07.2005",3, epic1);
        taskManager.getSubtasks().put(3L, subtask1);
        ArrayList<Subtask> subtaskList = epic1.getSubtaskList();
        subtaskList.add(subtask1);
        epic1.setSubtaskList(subtaskList);
        taskManager.getPrioritizedTasks().add(subtask1);
        taskManager.save();
        taskManager.getTasks().clear();
        taskManager.getSubtasks().clear();
        taskManager.getEpics().clear();
        taskManager.getPrioritizedTasks().clear();
        HistoryManager historyManager = taskManager.getHistoryManager();
        historyManager.remove(1L);
        historyManager.remove(2L);
        historyManager.remove(3L);
        taskManager = FileBackedTasksManager.loadFromFile(fileToSave);
        assertEquals(1, taskManager.getTasks().size());
        assertEquals(1, taskManager.getSubtasks().size());
        assertEquals(1, taskManager.getEpics().size());
        assertEquals(0, taskManager.getHistory().size());
        assertEquals(3, taskManager.getPrioritizedTasks().size());
    }

    @Test
    public void loadFromFileWithHistory() throws ManagerSaveException, IOException {
        Task task1 = new Task("a", "b", 1, "09.08.2002", 3);
        taskManager.getTasks().put(1L, task1);
        taskManager.getPrioritizedTasks().add(task1);
        taskManager.getHistoryManager().add(taskManager.getTasks().get(1L));
        Epic epic1 = new Epic("a", "b", 2);
        taskManager.getEpics().put(2L, epic1);
        taskManager.getPrioritizedTasks().add(epic1);
        taskManager.getHistoryManager().add(taskManager.getEpics().get(2L));
        Subtask subtask1 = new Subtask("a", "b", 3, "05.07.2005",3, epic1);
        taskManager.getSubtasks().put(3L, subtask1);
        ArrayList<Subtask> subtaskList = epic1.getSubtaskList();
        subtaskList.add(subtask1);
        epic1.setSubtaskList(subtaskList);
        taskManager.getPrioritizedTasks().add(subtask1);
        taskManager.getHistoryManager().add(taskManager.getSubtasks().get(3L));
        taskManager.save();
        taskManager.getTasks().clear();
        taskManager.getSubtasks().clear();
        taskManager.getEpics().clear();
        taskManager.getPrioritizedTasks().clear();
        HistoryManager historyManager = taskManager.getHistoryManager();
        historyManager.remove(1L);
        historyManager.remove(2L);
        historyManager.remove(3L);
        taskManager = FileBackedTasksManager.loadFromFile(fileToSave);
        assertEquals(1, taskManager.getTasks().size());
        assertEquals(1, taskManager.getSubtasks().size());
        assertEquals(1, taskManager.getEpics().size());
        assertEquals(3, taskManager.getHistory().size());
        assertEquals(3, taskManager.getPrioritizedTasks().size());
    }

    @Test
    public void loadFromFileWhenFileIsEmpty() throws IOException {
        taskManager = FileBackedTasksManager.loadFromFile(fileToSave);
        assertEquals(0, taskManager.getTasks().size());
        assertEquals(0, taskManager.getSubtasks().size());
        assertEquals(0, taskManager.getEpics().size());
        assertEquals(0, taskManager.getHistory().size());
        assertEquals(0, taskManager.getPrioritizedTasks().size());
    }

    @Test
    public void getHistory() throws ManagerSaveException {
        assertEquals(0, taskManager.getHistory().size());
        Task task = taskManager.createNewTask("a", "b", 1, "09.08.2002", 3);
        Epic epic = taskManager.createNewEpic("a", "b", 2);
        Subtask subtask = taskManager.createNewSubtask("a", "b", 3, "05.07.2005",
                3, epic);
        taskManager.getTask(1);
        taskManager.getSubtask(3);
        taskManager.getEpic(2);
        assertEquals(3, taskManager.getHistory().size());
        assertEquals(task, taskManager.getHistory().get(0));
        assertEquals(subtask, taskManager.getHistory().get(1));
        assertEquals(epic, taskManager.getHistory().get(2));
    }
}