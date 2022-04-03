package main;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.jdi.Type;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

public class HttpTaskServer {
    protected FileBackedTasksManager fileBacked = Managers.getBackup(new File("backup.csv"));

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpTaskServer httpTaskServer = new HttpTaskServer();
        new KVServer().start();
    }

    public HttpTaskServer() throws IOException, InterruptedException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/tasks/task", new TaskHandler());
        httpServer.createContext("/tasks/subtask", new SubtasksHandler());
        httpServer.createContext("/tasks/epic", new EpicsHandler());
        httpServer.createContext("/tasks/history", new HistoryHandler());
        httpServer.createContext("/tasks/", new PrioritizedHandler());
        httpServer.start();
        System.out.println("Сервер запущен.");

        /*HttpClient client = HttpClient.newHttpClient();
        URI getTasks = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(getTasks).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());*/
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
                        try {
                            if (fileBacked.getTasks().isEmpty()) {
                                response = "Задач нет.";
                                break;
                            }
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
                    final String json = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    final Task task = gson.fromJson(json, Task.class);
                    if (pathGet.contains("id=")) {
                        long id = Long.parseLong(pathGet.split("=")[1]);
                        try {
                            fileBacked.updateTask(id, task);
                            if (fileBacked.getTask(id).getName().equals(task.getName()) &&
                                fileBacked.getTask(id).getDescription().equals(task.getDescription()) &&
                                fileBacked.getTask(id).getStatus().equals(task.getStatus()) &&
                                fileBacked.getTask(id).getStartTime().equals(task.getStartTime()) &&
                                fileBacked.getTask(id).getDuration().equals(task.getDuration())) {
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
                        fileBacked.setTasks(task);
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
                            if (fileBacked.getTask(id) == null) {
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
                    System.out.println("Некорректный метод!");
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
        public Task read(JsonReader jsonReader) throws IOException {
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Task task = null;
            try {
                if (jsonObject.get("startTime") == null) {
                    task = fileBacked.createNewTask(jsonObject.get("name").getAsString(),
                                                    jsonObject.get("description").getAsString(),
                                                    jsonObject.get("id").getAsLong());
                } else {
                    task = fileBacked.createNewTask(jsonObject.get("name").getAsString(),
                                                    jsonObject.get("description").getAsString(),
                                                    jsonObject.get("id").getAsLong(),
                                                    jsonObject.get("startTime").getAsString(),
                                                    jsonObject.get("duration").getAsInt());
                }
                if (jsonObject.get("status") != null) {
                    task.setStatus(jsonObject.get("status").getAsString());
                }
            } catch (ManagerSaveException e) {
                e.printStackTrace();
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
                    if (pathGet.contains("id=")) {
                        long id = Long.parseLong(pathGet.split("=")[1]);
                        try {
                            if (fileBacked.getSubtasks().isEmpty()) {
                                response = "Подзадач нет.";
                                break;
                            }
                            response = gson.toJson(fileBacked.getSubtask(id));
                        } catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }
                        break;
                    } else if (pathGet.split("/")[2].contains("subtask")) {
                        response = gson.toJson(fileBacked.getSubtasks());
                        break;
                    }
                case "POST":
                    final String json = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    final Subtask subtask = gson.fromJson(json, Subtask.class);
                    if (pathGet.contains("id=")) {
                        long id = Long.parseLong(pathGet.split("=")[1]);
                        try {
                            fileBacked.updateSubtask(id, subtask);
                            if (fileBacked.getSubtask(id).getName().equals(subtask.getName()) &&
                                fileBacked.getSubtask(id).getDescription().equals(subtask.getDescription()) &&
                                fileBacked.getSubtask(id).getStatus().equals(subtask.getStatus()) &&
                                fileBacked.getSubtask(id).getStartTime().equals(subtask.getStartTime()) &&
                                fileBacked.getSubtask(id).getDuration().equals(subtask.getDuration()) &&
                                fileBacked.getSubtask(id).getEpic().equals(subtask.getEpic())) {
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
                        fileBacked.setSubtasks(subtask);
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
                            if (fileBacked.getSubtask(id) == null) {
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
                    System.out.println("Некорректный метод!");
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
        public Subtask read(JsonReader jsonReader) throws IOException {
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Subtask subtask = null;
            try {
                Epic epic = fileBacked.getEpic(jsonObject.get("epic").getAsLong());
                fileBacked.getHistoryManager().remove(epic.getId());
                if (jsonObject.get("startTime") == null) {
                    subtask = fileBacked.createNewSubtask(jsonObject.get("name").getAsString(),
                                                          jsonObject.get("description").getAsString(),
                                                          jsonObject.get("id").getAsLong(),
                                                          epic);
                } else {
                    subtask = fileBacked.createNewSubtask(jsonObject.get("name").getAsString(),
                                                          jsonObject.get("description").getAsString(),
                                                          jsonObject.get("id").getAsLong(),
                                                          jsonObject.get("startTime").getAsString(),
                                                          jsonObject.get("duration").getAsInt(),
                                                          epic);
                }
                if (jsonObject.get("status") != null) {
                    subtask.setStatus(jsonObject.get("status").getAsString());
                }
            } catch (ManagerSaveException e) {
                e.printStackTrace();
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
                            if (fileBacked.getEpic(id).getName().equals(epic.getName())
                                && fileBacked.getEpic(id).getDescription().equals(epic.getDescription())) {
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
                        fileBacked.setEpics(epic);
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
                            if (fileBacked.getEpic(id) == null) {
                                response = "Эпик " + id + " успешно удалён.";
                            }
                        } catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }
                        break;
                    } else if (pathGet.split("/")[2].equals("epic")) {
                            try {
                                fileBacked.removeAllEpics(fileBacked.getEpics());
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
                    System.out.println("Некорректный метод!");
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
        public Epic read(JsonReader jsonReader) throws IOException {
            JsonElement jsonElement = JsonParser.parseReader(jsonReader);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Epic epic = null;
            epic = new Epic(jsonObject.get("name").getAsString(),
                    jsonObject.get("description").getAsString(),
                    jsonObject.get("id").getAsLong());
            return epic;
        }
    }

    public class HistoryHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = null;
            Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Task.class, new TaskAdapter())
                        .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                        .registerTypeAdapter(Epic.class, new EpicAdapter())
                        .create();
            String method = httpExchange.getRequestMethod();
            String pathGet = httpExchange.getRequestURI().toString();
            if (method.equals("GET") && !fileBacked.getHistory().isEmpty()
                && pathGet.split("/")[2].equals("history")) {
                response = gson.toJson(fileBacked.getHistory());
            } else {
                response = "История просмотров пустая.";
            }
            httpExchange.sendResponseHeaders(200, 0);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    public class PrioritizedHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = null;
            Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Task.class, new TaskAdapter())
                        .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                        .registerTypeAdapter(Epic.class, new EpicAdapter())
                        .create();
            String method = httpExchange.getRequestMethod();
            String pathGet = httpExchange.getRequestURI().toString();
            String[] path = pathGet.split("/");
            if (method.equals("GET") && !fileBacked.getPrioritizedTasks().isEmpty()
                    && path[1].equals("tasks")) {
                response = gson.toJson(fileBacked.getPrioritizedTasks());
            } else {
                response = "Список пуст.";
            }
            httpExchange.sendResponseHeaders(200, 0);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}


