package main.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.HTTPTaskManager;
import main.ManagerSaveException;
import main.adapters.SubtaskAdapter;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SubtaskHandler implements HttpHandler {
    private final HTTPTaskManager httpTaskManager;
    private final SubtaskAdapter subtaskAdapter;

    public SubtaskHandler(HTTPTaskManager httpTaskManager, SubtaskAdapter subtaskAdapter) {
        this.httpTaskManager = httpTaskManager;
        this.subtaskAdapter = subtaskAdapter;
    }
    
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = null;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Subtask.class, subtaskAdapter)
                .create();
        String method = httpExchange.getRequestMethod();
        String pathGet = httpExchange.getRequestURI().toString();
        switch(method) {
            case "GET":
                if (pathGet.split("/")[2].equals("subtask") && pathGet.split("/").length == 3) {
                    if (httpTaskManager.getSubtasks().isEmpty()) {
                        response = "Подзадач нет.";
                        break;
                    }
                    response = gson.toJson(httpTaskManager.getSubtasks());
                    break;
                } else if (pathGet.split("/")[2].equals("subtask")
                        && pathGet.split("/")[3].equals("epic")
                        && pathGet.split("/")[4].contains("?id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    if (httpTaskManager.getSubtasks().isEmpty()) {
                        response = "Подзадач нет.";
                        break;
                    }
                    response = gson.toJson(httpTaskManager.getEpics().get(id).getSubtaskList());
                    break;
                } else if (pathGet.split("/")[3].contains("id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    if (!httpTaskManager.getSubtasks().containsKey(id)) {
                        response = "Подзадачи " + id + " нет.";
                        break;
                    }
                    try {
                        response = gson.toJson(httpTaskManager.getSubtask(id));
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            case "POST":
                final String json = new String(httpExchange.getRequestBody().readAllBytes(),
                        StandardCharsets.UTF_8);
                final Subtask subtask = gson.fromJson(json, Subtask.class);
                if (pathGet.contains("id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    try {
                        httpTaskManager.updateSubtask(id, subtask);
                        if (httpTaskManager.getSubtasks().get(id).getName().equals(subtask.getName())
                                && httpTaskManager.getSubtasks().get(id).getDescription().equals(subtask.getDescription())
                                && httpTaskManager.getSubtasks().get(id).getStatus().equals(subtask.getStatus())
                                && httpTaskManager.getSubtasks().get(id).getStartTime().equals(subtask.getStartTime())
                                && httpTaskManager.getSubtasks().get(id).getDuration().equals(subtask.getDuration())
                                && httpTaskManager.getSubtasks().get(id).getEpic().equals(subtask.getEpic())) {
                            response = "Подзадача " + id + " успешно обновлена.";
                            break;
                        }
                        response = "Не удалось обновить задачу " + id
                                + ". Возможно время выполнения задачи пересекается с другими задачами.";
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (pathGet.split("/")[2].equals("subtask")) {
                    try {
                        if (subtask.getStartTime().isEqual(Task.DEFAULT_DATE)) {
                            httpTaskManager.createNewSubtask(subtask.getName(),
                                    subtask.getDescription(),
                                    subtask.getId(), subtask.getEpic());
                        } else {
                            httpTaskManager.createNewSubtask(subtask.getName(),
                                    subtask.getDescription(),
                                    subtask.getId(),
                                    subtask.getStartTime().toString(),
                                    (int) subtask.getDuration().toDays(), subtask.getEpic());
                        }
                        httpTaskManager.setStatus(subtask.getStatus().toString(), subtask.getId());
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
                        if (!httpTaskManager.getSubtasks().containsKey(id)) {
                            response = "Подзадачи с id " + id + " нет.";
                            break;
                        }
                        httpTaskManager.removeSubtask(id);
                        if (httpTaskManager.getSubtasks().get(id) == null) {
                            response = "Подзадача " + id + " успешно удалена.";
                        }
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (pathGet.split("/")[2].equals("subtask")) {
                    try {
                        httpTaskManager.removeAllSubtasks(httpTaskManager.getSubtasks());
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    if (!httpTaskManager.getSubtasks().isEmpty()) {
                        response = "Что-то пошло не так, подзадачи не удалены.";
                        break;
                    }
                    response = "Подзадачи удалены.";
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
