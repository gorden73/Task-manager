package main;

import java.io.File;
import java.io.IOException;
import java.net.URI;

public final class Managers {
    public static HTTPTaskManager getDefault(URI uri) throws IOException, InterruptedException {
        return new HTTPTaskManager(uri);
    }

    public static FileBackedTasksManager getBackup(File file) throws IOException {
        if (file == null || file.length() == 0) {
            return new FileBackedTasksManager(file);
        }
        return FileBackedTasksManager.loadFromFile(file);
    }
}
