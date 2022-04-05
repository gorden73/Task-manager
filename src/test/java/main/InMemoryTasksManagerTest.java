package main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URI;

class InMemoryTasksManagerTest extends TaskManagerTest {
    @BeforeEach
    void start() throws IOException, InterruptedException {
        taskManager = Managers.getDefault(URI.create("http://localhost:8078"));
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