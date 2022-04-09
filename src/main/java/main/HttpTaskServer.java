package main;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HttpTaskServer {
    protected HTTPTaskManager fileBacked;
    protected HttpServer httpServer;

    public static void main(String[] args) throws IOException, InterruptedException, ManagerSaveException {
        new KVServer().start();
        HttpTaskServer server = new HttpTaskServer(Managers.getDefault(URI.create("http://localhost:8078")));
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
        httpServer.createContext("/tasks/task", new TaskHandler());
        httpServer.createContext("/tasks/subtask", new SubtasksHandler());
        httpServer.createContext("/tasks/epic", new EpicsHandler());
        httpServer.createContext("/tasks/subtask/epic/?id=", new SubtasksHandler());
        httpServer.createContext("/tasks/history", new HistoryHandler());
        httpServer.createContext("/tasks/", new PrioritizedHandler());
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
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Task.class, new TaskAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .create();
        HashMap<Long, Task> tasks = gson.fromJson(fileBacked.load("tasks"),
                                                  new TypeToken<HashMap<Long, Task>>() {}.getType());
        if (tasks != null) {
            for (Task task : tasks.values()) {
                fileBacked.getTasks().put(task.getId(), task);
                fileBacked.getPrioritizedTasks().add(task);
            }
        }
        HashMap<Long, Epic> epics = gson.fromJson(fileBacked.load("epics"),
                                                  new TypeToken<HashMap<Long, Epic>>() {}.getType());
        if (epics != null) {
            for (Epic epic : epics.values()) {
                fileBacked.getEpics().put(epic.getId(), epic);
                fileBacked.getPrioritizedTasks().add(epic);
            }
        }
        HashMap<Long, Subtask> subtasks = gson.fromJson(fileBacked.load("subtasks"),
                                                        new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        if (subtasks != null) {
            for (Subtask subtask : subtasks.values()) {
                fileBacked.getSubtasks().put(subtask.getId(), subtask);
                subtask.getEpic().getSubtaskList().add(subtask);
                fileBacked.getPrioritizedTasks().add(subtask);
            }
        }
        List<Long> historyId = gson.fromJson(fileBacked.load("history"),
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

    public class TaskHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = null;
            Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Task.class, new TaskAdapter())
                            .create();
            String method = httpExchange.getRequestMethod();
            String pathGet = httpExchange.getRequestURI().toString();
            switch(method) {
                case "GET":
                    if (pathGet.contains("id=")) {
                        long id = Long.parseLong(pathGet.split("=")[1]);
                        if (fileBacked.getTasks().isEmpty()) {
                            response = "Задач нет.";
                            break;
                        }
                        try {
                            response = gson.toJson(fileBacked.getTask(id));
                        } catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }
                        break;
                    } else if (pathGet.split("/")[2].contains("task")) {
                        response = gson.toJson(fileBacked.getTasks());
                        break;
                    }
                case "POST":
                    final String json = new String(httpExchange.getRequestBody().readAllBytes(),
                                                   StandardCharsets.UTF_8);
                    final Task task = gson.fromJson(json, Task.class);
                    if (pathGet.contains("id=")) {
                        long id = Long.parseLong(pathGet.split("=")[1]);
                        try {
                            fileBacked.updateTask(id, task);
                            if (fileBacked.getTasks().get(id).getName().equals(task.getName())
                                && fileBacked.getTasks().get(id).getDescription().equals(task.getDescription())
                                && fileBacked.getTasks().get(id).getStatus().equals(task.getStatus())
                                && fileBacked.getTasks().get(id).getStartTime().equals(task.getStartTime())
                                && fileBacked.getTasks().get(id).getDuration().equals(task.getDuration())) {
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
                                fileBacked.createNewTask(task.getName(),
                                        task.getDescription(),
                                        task.getId());
                            } else {
                                fileBacked.createNewTask(task.getName(),
                                        task.getDescription(),
                                        task.getId(),
                                        task.getStartTime().toString(),
                                        (int) task.getDuration().toDays());
                            }
                            fileBacked.setStatus(task.getStatus().toString(), task.getId());
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
                            if (!fileBacked.getTasks().containsKey(id)) {
                                response = "Задачи с id" + id + " нет.";
                                break;
                            }
                            fileBacked.removeTask(id);
                            if (fileBacked.getTasks().get(id) == null) {
                                response = "Задача " + id + " успешно удалёна.";
                            }
                        } catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }
                        break;
                    } else if (pathGet.split("/")[2].contains("task")) {
                        try {
                            fileBacked.removeAllTasks(fileBacked.getTasks());
                        } catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }
                        if (!fileBacked.getTasks().isEmpty()) {
                            response = "Что-то пошло не так, задачи не удалены.";
                            break;
                        }
                        response = "Задачи удалены.";
                        break;
                    }
                default:
                    System.out.println("Некорректный метод " + method);
                    httpExchange.sendResponseHeaders(404, 0);
            }
            httpExchange.sendResponseHeaders(200, 0);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    public class TaskAdapter extends TypeAdapter<Task> {
        @Override
        public void write(JsonWriter jsonWriter, Task task) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("Class").value("Task");
            jsonWriter.name("name").value(task.getName());
            jsonWriter.name("description").value(task.getDescription());
            jsonWriter.name("id").value(task.getId());
            jsonWriter.name("status").value(task.getStatus().toString());
            jsonWriter.name("startTime").value(task.getStartTime().toString());
            jsonWriter.name("duration").value(task.getDuration().toDays());
            jsonWriter.name("endTime").value(task.getEndTime().toString());
            jsonWriter.endObject();
        }

        @Override
        public Task read(JsonReader jsonReader) {
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Task task;
            if (jsonObject.get("startTime") == null) {
                task = new Task(jsonObject.get("name").getAsString(),
                                jsonObject.get("description").getAsString(),
                                jsonObject.get("id").getAsLong());
            } else {
                task = new Task(jsonObject.get("name").getAsString(),
                                jsonObject.get("description").getAsString(),
                                jsonObject.get("id").getAsLong(),
                                jsonObject.get("startTime").getAsString(),
                                jsonObject.get("duration").getAsInt());
            }
            if (jsonObject.get("status") != null) {
                task.setStatus(jsonObject.get("status").getAsString());
            }
            return task;
        }
    }

    public class SubtasksHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = null;
            Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                            .create();
            String method = httpExchange.getRequestMethod();
            String pathGet = httpExchange.getRequestURI().toString();
            switch(method) {
                case "GET":
                    if (pathGet.split("/")[2].equals("subtask") && pathGet.split("/").length == 3) {
                        response = gson.toJson(fileBacked.getSubtasks());
                        break;
                    } else if (pathGet.split("/")[2].equals("subtask")
                        && pathGet.split("/")[3].equals("epic")
                        && pathGet.split("/")[4].contains("?id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    if (fileBacked.getSubtasks().isEmpty()) {
                        response = "Подзадач нет.";
                        break;
                    }
                    response = gson.toJson(fileBacked.getEpics().get(id).getSubtaskList());
                    break;
                    } else if (pathGet.split("/")[2].contains("id=")) {
                    long id = Long.parseLong(pathGet.split("=")[1]);
                    if (fileBacked.getSubtasks().isEmpty()) {
                        response = "Подзадач нет.";
                        break;
                    }
                    try {
                        response = gson.toJson(fileBacked.getSubtask(id));
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
                            fileBacked.updateSubtask(id, subtask);
                            if (fileBacked.getSubtasks().get(id).getName().equals(subtask.getName())
                                && fileBacked.getSubtasks().get(id).getDescription().equals(subtask.getDescription())
                                && fileBacked.getSubtasks().get(id).getStatus().equals(subtask.getStatus())
                                && fileBacked.getSubtasks().get(id).getStartTime().equals(subtask.getStartTime())
                                && fileBacked.getSubtasks().get(id).getDuration().equals(subtask.getDuration())
                                && fileBacked.getSubtasks().get(id).getEpic().equals(subtask.getEpic())) {
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
                                fileBacked.createNewSubtask(subtask.getName(),
                                        subtask.getDescription(),
                                        subtask.getId(), subtask.getEpic());
                            } else {
                                fileBacked.createNewSubtask(subtask.getName(),
                                        subtask.getDescription(),
                                        subtask.getId(),
                                        subtask.getStartTime().toString(),
                                        (int) subtask.getDuration().toDays(), subtask.getEpic());
                            }
                            fileBacked.setStatus(subtask.getStatus().toString(), subtask.getId());
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
                            if (!fileBacked.getSubtasks().containsKey(id)) {
                                response = "Подзадачи с id " + id + " нет.";
                                break;
                            }
                            fileBacked.removeSubtask(id);
                            if (fileBacked.getSubtasks().get(id) == null) {
                                response = "Подзадача " + id + " успешно удалена.";
                            }
                        } catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }
                        break;
                    } else if (pathGet.split("/")[2].equals("subtask")) {
                        try {
                            fileBacked.removeAllSubtasks(fileBacked.getSubtasks());
                        } catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }
                        if (!fileBacked.getSubtasks().isEmpty()) {
                            response = "Что-то пошло не так, подзадачи не удалены.";
                            break;
                        }
                        response = "Подзадачи удалены.";
                        break;
                    }
                default:
                    System.out.println("Некорректный метод " + method);
                    httpExchange.sendResponseHeaders(404, 0);
            }
            httpExchange.sendResponseHeaders(200, 0);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    public class SubtaskAdapter extends TypeAdapter<Subtask> {
        @Override
        public void write(JsonWriter jsonWriter, Subtask subtask) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("Class").value("Subtask");
            jsonWriter.name("name").value(subtask.getName());
            jsonWriter.name("description").value(subtask.getDescription());
            jsonWriter.name("id").value(subtask.getId());
            jsonWriter.name("status").value(subtask.getStatus().toString());
            jsonWriter.name("startTime").value(subtask.getStartTime().toString());
            jsonWriter.name("duration").value(subtask.getDuration().toDays());
            jsonWriter.name("endTime").value(subtask.getEndTime().toString());
            jsonWriter.name("epicId").value(subtask.getEpic().getId());
            jsonWriter.endObject();
        }

        @Override
        public Subtask read(JsonReader jsonReader) {
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Subtask subtask;
            Epic epic = fileBacked.getEpics().get(jsonObject.get("epicId").getAsLong());
            if (jsonObject.get("startTime") == null) {
                subtask = new Subtask(jsonObject.get("name").getAsString(),
                                      jsonObject.get("description").getAsString(),
                                      jsonObject.get("id").getAsLong(),
                                      epic);
            } else {
                subtask = new Subtask(jsonObject.get("name").getAsString(),
                                      jsonObject.get("description").getAsString(),
                                      jsonObject.get("id").getAsLong(),
                                      jsonObject.get("startTime").getAsString(),
                                      jsonObject.get("duration").getAsInt(),
                                      epic);
                }
            if (jsonObject.get("status") != null) {
                subtask.setStatus(jsonObject.get("status").getAsString());
            }
            return subtask;
        }
    }

    public class EpicsHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = null;
            Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Epic.class, new EpicAdapter())
                            .create();
            String method = httpExchange.getRequestMethod();
            String pathGet = httpExchange.getRequestURI().toString();
            switch (method) {
                case "GET":
                    if (pathGet.contains("id=")) {
                        long id = Long.parseLong(pathGet.split("=")[1]);
                        try {
                            if (fileBacked.getEpics().isEmpty()) {
                                response = "Эпиков нет.";
                                break;
                            }
                            response = gson.toJson(fileBacked.getEpic(id));
                        } catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }
                        break;
                    } else if (pathGet.split("/")[2].contains("epic")) {
                        response = gson.toJson(fileBacked.getEpics());
                        break;
                    }
                case "POST":
                    final String json = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    final Epic epic = gson.fromJson(json, Epic.class);
                    if (pathGet.contains("id=")) {
                        long id = Long.parseLong(pathGet.split("=")[1]);
                        try {
                            fileBacked.updateEpic(id, epic);
                            if (fileBacked.getEpics().get(id).getName().equals(epic.getName())
                                && fileBacked.getEpics().get(id).getDescription().equals(epic.getDescription())) {
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
                            fileBacked.createNewEpic(epic.getName(), epic.getDescription(), epic.getId());
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
                            if (!fileBacked.getEpics().containsKey(id)) {
                                response = "Эпика с id" + id + " нет.";
                                break;
                            }
                            fileBacked.removeEpic(id);
                            if (fileBacked.getEpics().get(id) == null) {
                                response = "Эпик " + id + " успешно удалён.";
                            }
                        } catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }
                        break;
                    } else if (pathGet.split("/")[2].equals("epic")) {
                        try {
                            fileBacked.removeAllSubtasks(fileBacked.getSubtasks());
                        } catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }
                        if (!fileBacked.getEpics().isEmpty()) {
                            response = "Что-то пошло не так, эпики не удалены.";
                            break;
                        }
                        response = "Эпики удалены.";
                        break;
                    }
                default:
                    System.out.println("Некорректный метод " + method);
                    httpExchange.sendResponseHeaders(404, 0);
            }
            httpExchange.sendResponseHeaders(200, 0);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    public class EpicAdapter extends TypeAdapter<Epic> {
        @Override
        public void write(JsonWriter jsonWriter, Epic epic) throws IOException {
            jsonWriter.beginObject();
            jsonWriter.name("Class").value("Epic");
            jsonWriter.name("name").value(epic.getName());
            jsonWriter.name("description").value(epic.getDescription());
            jsonWriter.name("id").value(epic.getId());
            jsonWriter.name("status").value(epic.getStatus().toString());
            jsonWriter.name("startTime").value(epic.getStartTime().toString());
            jsonWriter.name("duration").value(epic.getDuration().toDays());
            jsonWriter.name("endTime").value(epic.getEndTime().toString());
            jsonWriter.name("subtaskList");
            jsonWriter.beginArray();
            for (Subtask sub : epic.getSubtaskList()) {
                jsonWriter.value(sub.getId());
            }
            jsonWriter.endArray();
            jsonWriter.endObject();
        }

        @Override
        public Epic read(JsonReader jsonReader) {
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Epic epic;
            epic = new Epic(jsonObject.get("name").getAsString(),
                    jsonObject.get("description").getAsString(),
                    jsonObject.get("id").getAsLong());
            return epic;
        }
    }

    public class HistoryHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response;
            Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Task.class, new TaskAdapter())
                        .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                        .registerTypeAdapter(Epic.class, new EpicAdapter())
                        .create();
            String method = httpExchange.getRequestMethod();
            String pathGet = httpExchange.getRequestURI().toString();
            if (method.equals("GET") && pathGet.split("/")[2].equals("history")) {
                response = gson.toJson(fileBacked.getHistory());
                httpExchange.sendResponseHeaders(200, 0);
            } else {
                response = "/history ждёт GET-запрос, а получил  " + method;
                httpExchange.sendResponseHeaders(404, 0);
            }
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    public class PrioritizedHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response;
            Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Task.class, new TaskAdapter())
                        .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                        .registerTypeAdapter(Epic.class, new EpicAdapter())
                        .create();
            String method = httpExchange.getRequestMethod();
            String pathGet = httpExchange.getRequestURI().toString();
            String[] path = pathGet.split("/");
            if (method.equals("GET") && path[1].equals("tasks")) {
                response = gson.toJson(fileBacked.getPrioritizedTasks());
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
}


