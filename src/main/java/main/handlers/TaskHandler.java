package main.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.HTTPTaskManager;
import main.ManagerSaveException;
import main.adapters.TaskAdapter;
import tasktracker.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class TaskHandler implements HttpHandler {
    private HTTPTaskManager httpTaskManager;
    private TaskAdapter taskAdapter;

    public TaskHandler(HTTPTaskManager httpTaskManager, TaskAdapter taskAdapter) {
        this.httpTaskManager = httpTaskManager;
        this.taskAdapter = taskAdapter;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = null;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Task.class, taskAdapter)
                .create();
        String method = httpExchange.getRequestMethod();
        String pathGet = httpExchange.getRequestURI().toString();
        switch(method) {
            case "GET":
                if (pathGet.contains("id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    if (!httpTaskManager.getTasks().containsKey(id)) {
                        response = "Задачи " + id + " нет.";
                        break;
                    }
                    try {
                        response = gson.toJson(httpTaskManager.getTask(id));
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (pathGet.split("/")[2].contains("task")) {
                    if (httpTaskManager.getTasks().isEmpty()) {
                        response = "Задач нет.";
                        break;
                    }
                    response = gson.toJson(httpTaskManager.getTasks());
                    break;
                }
            case "POST":
                final String json = new String(httpExchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8);
                final Task task = gson.fromJson(json, Task.class);
                if (pathGet.contains("id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    try {
                        httpTaskManager.updateTask(id, task);
                        if (httpTaskManager.getTasks().get(id).getName().equals(task.getName())
                                && httpTaskManager.getTasks().get(id).getDescription().equals(task.getDescription())
                                && httpTaskManager.getTasks().get(id).getStatus().equals(task.getStatus())
                                && httpTaskManager.getTasks().get(id).getStartTime().equals(task.getStartTime())
                                && httpTaskManager.getTasks().get(id).getDuration().equals(task.getDuration())) {
                            response = "Задача " + id + " успешно обновлена.";
                            break;
                        }
                        response = "Не удалось обновить задачу " + id
                                + ". Возможно время выполнения задачи пересекается с другими задачами.";
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (pathGet.split("/")[2].contains("task")) {
                    try {
                        if (task.getStartTime().isEqual(Task.DEFAULT_DATE)) {
                            httpTaskManager.createNewTask(task.getName(),
                                    task.getDescription(),
                                    task.getId());
                        } else {
                            httpTaskManager.createNewTask(task.getName(),
                                    task.getDescription(),
                                    task.getId(),
                                    task.getStartTime().toString(),
                                    (int) task.getDuration().toDays());
                        }
                        httpTaskManager.setStatus(task.getStatus().toString(), task.getId());
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    response = "Задача добавлена.";
                    break;
                }
            case "DELETE":
                if (pathGet.contains("id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    try {
                        if (!httpTaskManager.getTasks().containsKey(id)) {
                            response = "Задачи с id" + id + " нет.";
                            break;
                        }
                        httpTaskManager.removeTask(id);
                        if (httpTaskManager.getTasks().get(id) == null) {
                            response = "Задача " + id + " успешно удалена.";
                        }
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (pathGet.split("/")[2].contains("task")) {
                    try {
                        httpTaskManager.removeAllTasks(httpTaskManager.getTasks());
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    if (!httpTaskManager.getTasks().isEmpty()) {
                        response = "Что-то пошло не так, задачи не удалены.";
                        break;
                    }
                    response = "Задачи удалены.";
                    break;
                }
            default:
                response = "Некорректный метод " + method;
                httpExchange.sendResponseHeaders(404, 0);
        }
        httpExchange.sendResponseHeaders(200, 0);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
