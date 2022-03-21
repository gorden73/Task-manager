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

   /* @Test
    public void save() throws ManagerSaveException {
        try (FileWriter writer = new FileWriter(fileToSave, StandardCharsets.UTF_8)) {
            writer.append("id,type,name,status,description,startTime,duration,endTime,epic" + "\n");
            for (Task task : fileBacked.getTasks().values()) {
                writer.write(toString(task) + "\n");
            }
            for (Task task : fileBacked.getEpics().values()) {
                writer.write(toString(task) + "\n");
            }
            for (Task task : fileBacked.getSubtasks().values()) {
                writer.write(toString(task) + "\n");
            }
            writer.append(" " + "\n");
            for (Task history : fileBacked.getHistory()) {
                Long id1 = history.getId();
                writer.append(id1 + ",");
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }*/

    @Test
    public void loadFromFile() {

    }

    @Test
    public void getHistory() {

    }
}