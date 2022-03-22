package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTasksManagerTest /*extends TaskManagerTest */{
    FileBackedTasksManager fileBacked;
    File fileToSave;

    @BeforeEach
    void start() throws IOException{
        fileToSave = new File("backup.csv");
        fileBacked = Managers.getBackup(fileToSave);
    }


    @Test
    public void getPrioritizedTasks() {
        int size = fileBacked.sortedTasks.size();
        assertEquals(0, size, "Отсортированный список должен быть пустым.");
        Set<Task> taskSet = fileBacked.getPrioritizedTasks();
        taskSet.add(new Task("A", "B", 1));
        assertEquals(1, taskSet.size(), "В списке должна быть 1 задача.");
        taskSet.add(new Epic("D", "C", 2));
        assertEquals(2, taskSet.size(), "В списке должно быть 2 задачи.");
        taskSet.clear();
        assertEquals(0, size, "Отсортированный список должен быть пустым.");
    }

    @Test
    public void save() throws ManagerSaveException, IOException {
        Task task1 = new Task("a", "b", 1, "09.08.2002", 3);
        fileBacked.getTasks().put(1L, task1);
        fileBacked.sortedTasks.add(task1);
        fileBacked.getHistoryManager().add(fileBacked.getTasks().get(1L));
        Epic epic1 = new Epic("a", "b", 2);
        fileBacked.getEpics().put(2L, epic1);
        fileBacked.sortedTasks.add(epic1);
        fileBacked.getHistoryManager().add(fileBacked.getEpics().get(2L));
        Subtask subtask1 = new Subtask("a", "b", 3, "05.07.2005",3, epic1);
        fileBacked.getSubtasks().put(3L, subtask1);
        ArrayList<Subtask> subtaskList = epic1.getSubtaskList();
        subtaskList.add(subtask1);
        epic1.setSubtaskList(subtaskList);
        fileBacked.sortedTasks.add(subtask1);
        fileBacked.getHistoryManager().add(fileBacked.getSubtasks().get(3L));
        fileBacked.save();
        fileBacked.getTasks().clear();
        fileBacked.getSubtasks().clear();
        fileBacked.getEpics().clear();
        HistoryManager historyManager = fileBacked.getHistoryManager();
        historyManager.remove(1L);
        historyManager.remove(2L);
        historyManager.remove(3L);
        assertEquals(0, fileBacked.getTasks().size());
        assertEquals(0, fileBacked.getSubtasks().size());
        assertEquals(0, fileBacked.getEpics().size());
        assertEquals(0, fileBacked.getHistory().size());
        FileBackedTasksManager fileBacked1 = FileBackedTasksManager.loadFromFile(fileToSave);
        assertEquals(1, fileBacked1.getTasks().size());
        assertEquals(1, fileBacked1.getSubtasks().size());
        assertEquals(1, fileBacked1.getEpics().size());
        assertEquals(3, fileBacked1.getHistory().size());
        assertEquals(task1, fileBacked1.getTask(1));
        assertEquals(subtask1, fileBacked1.getSubtask(3));
        assertEquals(epic1, fileBacked1.getEpic(2));
    }

    @Test
    public void loadFromFile() {

    }

    @Test
    public void getHistory() throws ManagerSaveException {
        assertEquals(0, fileBacked.getHistory().size());
        Task task = fileBacked.createNewTask("a", "b", 1, "09.08.2002", 3);
        Epic epic = fileBacked.createNewEpic("a", "b", 2);
        Subtask subtask = fileBacked.createNewSubtask("a", "b", 3, "05.07.2005",
                3, epic);
        fileBacked.getTask(1);
        fileBacked.getSubtask(3);
        fileBacked.getEpic(2);
        assertEquals(3, fileBacked.getHistory().size());
        assertEquals(task, fileBacked.getHistory().get(0));
        assertEquals(subtask, fileBacked.getHistory().get(1));
        assertEquals(epic, fileBacked.getHistory().get(2));
    }
}