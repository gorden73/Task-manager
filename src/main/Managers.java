package main;

public final class Managers {
    public static InMemoryTasksManager getDefault() {
        return new InMemoryTasksManager();
    }
}
