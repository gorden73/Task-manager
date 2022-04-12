package main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

class InMemoryTasksManagerTest extends TaskManagerTest<InMemoryTasksManager> {

    @BeforeEach
    void start() {
       taskManager = new InMemoryTasksManager();
    }

    @AfterEach
    void end() {
        taskManager.getTasks().clear();
        taskManager.getSubtasks().clear();
        taskManager.getEpics().clear();
        taskManager.getHistory().clear();
    }
}