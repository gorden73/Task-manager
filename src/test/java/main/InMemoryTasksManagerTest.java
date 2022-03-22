package main;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.Task;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTasksManagerTest extends TaskManagerTest {
    @BeforeEach
    void start() throws IOException {
        taskManager = Managers.getDefault();
    }

    @AfterEach
    void end() throws ManagerSaveException {
        taskManager.getTasks().clear();
        taskManager.getSubtasks().clear();
        taskManager.getEpics().clear();
        taskManager.getHistory().clear();
        taskManager.sortedTasks.clear();
        taskManager.save();
    }
}