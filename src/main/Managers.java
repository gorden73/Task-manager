package main;

import java.io.File;
import java.io.IOException;

public final class Managers {
    public static InMemoryTasksManager getDefault() {
        return new InMemoryTasksManager();
    }

    public static FileBackedTasksManager getBackup(File file) throws IOException {
        if (file == null || file.length() == 0) {
            return new FileBackedTasksManager(file);
        }
        return FileBackedTasksManager.loadFromFile(file);
    }
}
