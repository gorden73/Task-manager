package main.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.HTTPTaskManager;
import main.adapters.EpicAdapter;
import main.adapters.SubtaskAdapter;
import main.adapters.TaskAdapter;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.IOException;
import java.io.OutputStream;

public class PrioritizedHandler implements HttpHandler {
    private final HTTPTaskManager httpTaskManager;
    private final TaskAdapter taskAdapter;
    private final SubtaskAdapter subtaskAdapter;
    private final EpicAdapter epicAdapter;

    public PrioritizedHandler(HTTPTaskManager httpTaskManager, TaskAdapter taskAdapter, SubtaskAdapter subtaskAdapter,
                              EpicAdapter epicAdapter) {
        this.httpTaskManager = httpTaskManager;
        this.taskAdapter = taskAdapter;
        this.subtaskAdapter = subtaskAdapter;
        this.epicAdapter = epicAdapter;
    }
    
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Task.class, taskAdapter)
                .registerTypeAdapter(Subtask.class, subtaskAdapter)
                .registerTypeAdapter(Epic.class, epicAdapter)
                .create();
        String method = httpExchange.getRequestMethod();
        String pathGet = httpExchange.getRequestURI().toString();
        String[] path = pathGet.split("/");
        if (method.equals("GET") && path[1].equals("tasks")) {
            response = gson.toJson(httpTaskManager.getPrioritizedTasks());
            httpExchange.sendResponseHeaders(200, 0);
        } else {
            response = "/tasks/ ждёт GET-запрос, а получил " + method;
            httpExchange.sendResponseHeaders(404, 0);
        }
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
