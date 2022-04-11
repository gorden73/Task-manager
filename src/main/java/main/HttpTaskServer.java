package main;

import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpServer;
import main.handlers.*;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HttpTaskServer {
    protected HTTPTaskManager fileBacked;
    private final HttpServer httpServer;

    public static void main(String[] args) throws IOException, InterruptedException, ManagerSaveException {
        new KVServer().start();
        HttpTaskServer server = new HttpTaskServer(Managers.getDefault());
        server.start();
        server.fileBacked.createNewTask("название", "описание", 1);
        Epic epic = server.fileBacked.createNewEpic("название1", "описание1", 2);
        server.fileBacked.createNewSubtask("название2", "описание2", 3, epic);
        server.fileBacked.getTask(1);
        server.fileBacked.getSubtask(3);
        server.fileBacked.getEpic(2);
        server.fileBacked.getTasks().clear();
        server.fileBacked.getSubtasks().clear();
        server.fileBacked.getEpics().clear();
        server.fileBacked.getHistory().clear();
        server.fileBacked.getPrioritizedTasks().clear();
        server.backup();
    }

    public HttpTaskServer(HTTPTaskManager taskManager) throws IOException {
        fileBacked = taskManager;
        httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        createContext();
    }

    private void createContext() {
        httpServer.createContext("/tasks/task", new TaskHandler(fileBacked, fileBacked.getTaskAdapter()));
        httpServer.createContext("/tasks/subtask", new SubtaskHandler(fileBacked, fileBacked.getSubtaskAdapter()));
        httpServer.createContext("/tasks/epic", new EpicHandler(fileBacked, fileBacked.getEpicAdapter()));
        httpServer.createContext("/tasks/history", new HistoryHandler(fileBacked, fileBacked.getTaskAdapter(),
                                                                           fileBacked.getSubtaskAdapter(),
                                                                           fileBacked.getEpicAdapter()));
        httpServer.createContext("/tasks/", new PrioritizedHandler(fileBacked, fileBacked.getTaskAdapter(),
                fileBacked.getSubtaskAdapter(), fileBacked.getEpicAdapter()));
    }

    public void start() throws IOException, InterruptedException {
        httpServer.start();
        System.out.println("TaskServer запущен.");
        backup();
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("TaskServer остановлен.");
    }

    public void backup() throws IOException, InterruptedException {
        HashMap<Long, Task> tasks = fileBacked.getGson().fromJson(fileBacked.load("tasks"),
                                                  new TypeToken<HashMap<Long, Task>>() {}.getType());
        if (tasks != null) {
            for (Task task : tasks.values()) {
                fileBacked.getTasks().put(task.getId(), task);
                fileBacked.getPrioritizedTasks().add(task);
            }
        }
        HashMap<Long, Epic> epics = fileBacked.getGson().fromJson(fileBacked.load("epics"),
                                                  new TypeToken<HashMap<Long, Epic>>() {}.getType());
        if (epics != null) {
            for (Epic epic : epics.values()) {
                fileBacked.getEpics().put(epic.getId(), epic);
                fileBacked.getPrioritizedTasks().add(epic);
            }
        }
        HashMap<Long, Subtask> subtasks = fileBacked.getGson().fromJson(fileBacked.load("subtasks"),
                                                        new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        if (subtasks != null) {
            for (Subtask subtask : subtasks.values()) {
                fileBacked.getSubtasks().put(subtask.getId(), subtask);
                subtask.getEpic().getSubtaskList().add(subtask);
                fileBacked.getPrioritizedTasks().add(subtask);
            }
        }
        List<Long> historyId = fileBacked.getGson().fromJson(fileBacked.load("history"),
                new TypeToken<ArrayList<Long>>() {}.getType());
        if (historyId != null) {
            for (Long id : historyId) {
                if (fileBacked.getTasks().containsKey(id)) {
                    fileBacked.getHistoryManager().add(fileBacked.getTasks().get(id));
                } else if (fileBacked.getSubtasks().containsKey(id)) {
                    fileBacked.getHistoryManager().add(fileBacked.getSubtasks().get(id));
                } else if (fileBacked.getEpics().containsKey(id)) {
                    fileBacked.getHistoryManager().add(fileBacked.getEpics().get(id));
                }
            }
        }
    }
}