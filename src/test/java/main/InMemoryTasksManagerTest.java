package main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

class InMemoryTasksManagerTest extends TaskManagerTest {
    @BeforeEach
    void start() {
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