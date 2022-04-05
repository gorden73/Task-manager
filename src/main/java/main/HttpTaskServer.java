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

import java.awt.*;
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
    protected HTTPTaskManager fileBacked = Managers.getDefault(URI.create("http://localhost:8078"));
    protected HttpServer httpServer;

    public static void main(String[] args) throws IOException, InterruptedException, ManagerSaveException {
        new KVServer().start();
        HttpTaskServer server = new HttpTaskServer();
        server.start();
        server.stop(10); //что-то не работает
        server.start();
    }

    public HttpTaskServer() throws IOException, InterruptedException {
        httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        createContext();
    }

    private void createContext() {
        httpServer.createContext("/tasks/task", new TaskHandler());
        httpServer.createContext("/tasks/subtask", new SubtasksHandler());
        httpServer.createContext("/tasks/epic", new EpicsHandler());
        httpServer.createContext("/tasks/history", new HistoryHandler());
        httpServer.createContext("/tasks/", new PrioritizedHandler());
    }

    public void start() throws IOException, ManagerSaveException, InterruptedException {
        httpServer.start();
        backup(); //восстановление состояния менеджера
        System.out.println("Сервер запущен.");
    }

    public void stop(int seconds) {
        httpServer.stop(seconds);
        System.out.println("Сервер остановлен.");
    }

    public void backup() throws IOException, InterruptedException, ManagerSaveException {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Task.class, new TaskAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .create();
        if (fileBacked.load("tasks") != null) {
            HashMap<Long, Task> tasks = gson.fromJson(fileBacked.load("tasks"),
                    new TypeToken<HashMap<Long, Task>>() {
                    }.getType());
            for (Task task : tasks.values()) {
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
            }
        }
        if (fileBacked.load("epics") != null) {
            HashMap<Long, Epic> epics = gson.fromJson(fileBacked.load("epics"),
                    new TypeToken<HashMap<Long, Task>>() {
                    }.getType());
            for (Epic epic : epics.values()) {
                fileBacked.createNewEpic(epic.getName(), epic.getDescription(), epic.getId());
            }
        }
        if (fileBacked.load("subtasks") != null) {
            HashMap<Long, Subtask> subtasks = gson.fromJson(fileBacked.load("subtasks"),
                    new TypeToken<HashMap<Long, Task>>() {
                    }.getType());
            for (Subtask subtask : subtasks.values()) {
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
            }
        }
        if (fileBacked.load("history") != null) {
            List<Task> history = gson.fromJson(fileBacked.load("history"),
                    new TypeToken<List<Task>>() {
                    }.getType());
            for (Task task : history) {
                fileBacked.getHistoryManager().add(task);
            }
        }
        if (fileBacked.load("sortedTasks") != null) {
            Set<Task> sortedTasks = gson.fromJson(fileBacked.load("sortedTask"),
                    new TypeToken<Set<Task>>() {
                    }.getType());
            for (Task task : sortedTasks) {
                fileBacked.sortedTasks.add(task);
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
                        try {
                            if (fileBacked.getTasks().isEmpty()) {
                                response = "Задач нет.";
                                break;
                            }
                            HashMap<Long, Task> tasks = gson.fromJson(fileBacked.load("tasks"),
                                                                      new TypeToken<HashMap<Long, Task>>() {}.getType());

                            fileBacked.getHistoryManager().add(tasks.get(id));///
                            fileBacked.save();///

                            response = gson.toJson(tasks.get(id));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    } else if (pathGet.split("/")[2].contains("task")) {
                        try {
                            response = fileBacked.load("tasks");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
                    if (pathGet.contains("id=")) {
                        long id = Long.parseLong(pathGet.split("=")[1]);
                        try {
                            if (fileBacked.getSubtasks().isEmpty()) {
                                response = "Подзадач нет.";
                                break;
                            }
                            HashMap<Long, Subtask> subtasks = gson.fromJson(fileBacked.load("subtasks"),
                                    new TypeToken<HashMap<Long, Subtask>>() {}.getType());

                            fileBacked.getHistoryManager().add(subtasks.get(id));///

                            fileBacked.save();///

                            response = gson.toJson(subtasks.get(id));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //response = gson.toJson(fileBacked.getSubtask(id));
                        /*} catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }*/
                        break;
                    } else if (pathGet.split("/")[2].contains("subtask")) {
                        //response = gson.toJson(fileBacked.getSubtasks());
                        try {
                            response = fileBacked.load("subtasks");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
                        //fileBacked.setSubtasks(subtask);
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
                Epic epic = fileBacked.getEpic(jsonObject.get("epic").getAsLong()); ///возможно здесь надо просто создать new Epic (не через createNewEpic)
                fileBacked.getHistoryManager().remove(epic.getId());
                /*if (jsonObject.get("startTime") == null) {
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
                }*/
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
                            HashMap<Long, Epic> epics = gson.fromJson(fileBacked.load("epics"),
                                    new TypeToken<HashMap<Long, Epic>>() {}.getType());

                            /*epics.get(id).getStartTime();///
                            epics.get(id).getDuration();///*/

                            fileBacked.getHistoryManager().add(epics.get(id));///
                            fileBacked.save();///

                            response = gson.toJson(epics.get(id));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                            //response = gson.toJson(fileBacked.getEpic(id));
                        /*} catch (ManagerSaveException e) {
                            e.printStackTrace();
                        }*/
                        break;
                    } else if (pathGet.split("/")[2].contains("epic")) {
                        //response = gson.toJson(fileBacked.getEpics());
                        try {
                            response = fileBacked.load("epics");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                case "POST":
                    final String json = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    final Epic epic = gson.fromJson(json, Epic.class);
                    if (pathGet.contains("id=")) {
                        long id = Long.parseLong(pathGet.split("=")[1]);
                        try {
                            fileBacked.updateEpic(id, epic);
                            //fileBacked.getEpics().get(epic.getId()).getStatus();
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
                        //fileBacked.setEpics(epic);
                        try {
                            fileBacked.createNewEpic(epic.getName(), epic.getDescription(), epic.getId());

                            fileBacked.getEpics().get(epic.getId()).getStartTime();///
                            fileBacked.getEpics().get(epic.getId()).getDuration();///

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
            if (method.equals("GET") /*&& !fileBacked.getHistory().isEmpty()*/
                && pathGet.split("/")[2].equals("history")) {
                //response = gson.toJson(fileBacked.getHistory());

                try {
                    response = fileBacked.load("history");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                //response = "История просмотров пустая.";
                response = "Неверный запрос.";
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
            if (method.equals("GET") /*&& !fileBacked.getPrioritizedTasks().isEmpty()*/
                    && path[1].equals("tasks")) {
                //response = gson.toJson(fileBacked.getPrioritizedTasks());

                try {
                    response = fileBacked.load("sortedTasks");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                //response = "Список пуст.";

                response = "Неверный запрос.";
            }
            httpExchange.sendResponseHeaders(200, 0);
            try (OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}


