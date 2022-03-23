package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    HistoryManager historyManager;
    TaskManager taskManager;
    Task task;
    Epic epic;
    Subtask subtask;

    @BeforeEach
    void start() throws ManagerSaveException {
        historyManager = new InMemoryHistoryManager();
        taskManager = new InMemoryTasksManager();
        task = taskManager.createNewTask("a", "b", 1, "09.08.2002", 3);
        epic = taskManager.createNewEpic("a", "b", 2);
        subtask = taskManager.createNewSubtask("a", "b", 3, "05.07.2005",
                3, epic);
    }

    @Test
    void add() throws ManagerSaveException {
        assertEquals(0, historyManager.getHistory().size());
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size());
        assertEquals(task, historyManager.getHistory().get(0));
        historyManager.add(epic);
        assertEquals(2, historyManager.getHistory().size());
        assertEquals(epic, historyManager.getHistory().get(1));
        historyManager.add(subtask);
        assertEquals(3, historyManager.getHistory().size());
        assertEquals(subtask, historyManager.getHistory().get(2));
        historyManager.add(task);
        assertEquals(3, historyManager.getHistory().size());
    }

    @Test
    void remove() {
        assertEquals(0, historyManager.getHistory().size());
        historyManager.remove(1L);
        assertEquals(0, historyManager.getHistory().size());
        historyManager.add(epic);
        assertEquals(1, historyManager.getHistory().size());
        historyManager.remove(1L);
        assertEquals(1, historyManager.getHistory().size());
        historyManager.remove(2L);
        assertEquals(0, historyManager.getHistory().size());
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);
        assertEquals(3, historyManager.getHistory().size());
        historyManager.remove(3L);
        assertEquals(2, historyManager.getHistory().size());
        historyManager.remove(1L);
        assertEquals(1, historyManager.getHistory().size());
        historyManager.add(task);
        historyManager.add(subtask);
        historyManager.remove(1L);
        assertEquals(2, historyManager.getHistory().size());
    }

    @Test
    void getHistory() {
        assertEquals(0, historyManager.getHistory().size());
        historyManager.add(task);
        List<Task> list = historyManager.getHistory();
        assertEquals(task, list.get(0));
        historyManager.add(epic);
        list = historyManager.getHistory();
        assertEquals(epic, list.get(1));
        historyManager.add(subtask);
        list = historyManager.getHistory();
        assertEquals(subtask, list.get(2));
        assertEquals(3, historyManager.getHistory().size());
        historyManager.add(task);
        assertEquals(3, historyManager.getHistory().size());
    }
}