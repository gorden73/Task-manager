package main.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.HTTPTaskManager;
import main.ManagerSaveException;
import main.adapters.EpicAdapter;
import tasktracker.Epic;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class EpicHandler implements HttpHandler {
    private HTTPTaskManager httpTaskManager;
    private EpicAdapter epicAdapter;

    public EpicHandler(HTTPTaskManager httpTaskManager, EpicAdapter epicAdapter) {
        this.httpTaskManager = httpTaskManager;
        this.epicAdapter = epicAdapter;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = null;
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Epic.class, epicAdapter)
                .create();
        String method = httpExchange.getRequestMethod();
        String pathGet = httpExchange.getRequestURI().toString();
        switch (method) {
            case "GET":
                if (pathGet.contains("id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    try {
                        if (!httpTaskManager.getEpics().containsKey(id)) {
                            response = "Эпика " + id + " нет.";
                            break;
                        }
                        response = gson.toJson(httpTaskManager.getEpic(id));
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (pathGet.split("/")[2].contains("epic")) {
                    if (httpTaskManager.getEpics().isEmpty()) {
                        response = "Эпиков нет.";
                        break;
                    }
                    response = gson.toJson(httpTaskManager.getEpics());
                    break;
                }
            case "POST":
                final String json = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                final Epic epic = gson.fromJson(json, Epic.class);
                if (pathGet.contains("id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    try {
                        httpTaskManager.updateEpic(id, epic);
                        if (httpTaskManager.getEpics().get(id).getName().equals(epic.getName())
                                && httpTaskManager.getEpics().get(id).getDescription().equals(epic.getDescription())) {
                            response = "Эпик " + id + " успешно обновлен.";
                            break;
                        }
                        response = "Не удалось обновить эпик " + id
                                + ". Возможно время выполнения задачи пересекается с другими задачами.";
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (pathGet.split("/")[2].contains("epic")) {
                    try {
                        httpTaskManager.createNewEpic(epic.getName(), epic.getDescription(), epic.getId());
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    response = "Эпик добавлен.";
                    break;
                }
            case "DELETE":
                if (pathGet.contains("id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    try {
                        if (!httpTaskManager.getEpics().containsKey(id)) {
                            response = "Эпика с id" + id + " нет.";
                            break;
                        }
                        httpTaskManager.removeEpic(id);
                        if (httpTaskManager.getEpics().get(id) == null) {
                            response = "Эпик " + id + " успешно удалён.";
                        }
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    break;
                } else if (pathGet.split("/")[2].equals("epic")) {
                    try {
                        httpTaskManager.removeAllSubtasks(httpTaskManager.getSubtasks());
                    } catch (ManagerSaveException e) {
                        e.printStackTrace();
                    }
                    if (!httpTaskManager.getEpics().isEmpty()) {
                        response = "Что-то пошло не так, эпики не удалены.";
                        break;
                    }
                    response = "Эпики удалены.";
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
