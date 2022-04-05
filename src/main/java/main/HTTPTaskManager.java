package main;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class HTTPTaskManager extends FileBackedTasksManager {
    KVTaskClient client;
    Gson gson;

    public HTTPTaskManager(URI uri) throws IOException, InterruptedException {
        client = new KVTaskClient(uri);
        gson = new GsonBuilder()
                .registerTypeAdapter(Task.class, new TaskAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .create();
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
                Epic epic = getEpic(jsonObject.get("epic").getAsLong());
                getHistoryManager().remove(epic.getId());
                if (jsonObject.get("startTime") == null) {
                    subtask = new Subtask(jsonObject.get("name").getAsString(),
                                               jsonObject.get("description").getAsString(),
                                               jsonObject.get("id").getAsLong(), epic);
                } else {
                    subtask = new Subtask(jsonObject.get("name").getAsString(),
                                               jsonObject.get("description").getAsString(),
                                               jsonObject.get("id").getAsLong(),
                                               jsonObject.get("startTime").getAsString(),
                                               jsonObject.get("duration").getAsInt(), epic);
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

    /*public List<Task> load(String key) throws IOException, InterruptedException {
        List<Task> list = new ArrayList<>();

        switch (key) {
            case "tasks":
                list = gson.fromJson(client.load(key), new TypeToken<ArrayList<Task>>() {}.getType());
                break;
            case "subtasks":
                list = gson.fromJson(client.load(key), new TypeToken<ArrayList<Subtask>>() {}.getType());
                break;
            case "epics":
                list = gson.fromJson(client.load(key), new TypeToken<ArrayList<Epic>>() {}.getType());
                break;
            case "history":
                list = gson.fromJson(client.load(key), new TypeToken<ArrayList<Task>>() {}.getType());
                break;
        }

        return list;
    }*/

    public String load(String key) throws IOException, InterruptedException {
        return client.load(key);
    }

    @Override
    public void save() {
        try {
            super.save();
            client.put("tasks", gson.toJson(getTasks()));
            client.put("epics", gson.toJson(getEpics()));
            client.put("subtasks", gson.toJson(getSubtasks()));
            client.put("history", gson.toJson(getHistory()));
            client.put("sortedTasks", gson.toJson(getPrioritizedTasks()));
        } catch (IOException | InterruptedException | ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setStatus(String status, long id) throws ManagerSaveException {
        super.setStatus(status, id);
        save();
    }

    @Override
    public Set<Task> getPrioritizedTasks() {
        return super.getPrioritizedTasks();
    }

    @Override
    public HashMap<Long, Task> getTasks() {
       return super.getTasks();
    }

    @Override
    public HashMap<Long, Subtask> getSubtasks() {
        return super.getSubtasks();
    }

    @Override
    public HashMap<Long, Epic> getEpics() {
        return super.getEpics();
    }

    @Override
    public Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic) throws ManagerSaveException {
        Subtask subtask = super.createNewSubtask(inputName, inputDescription, id, epic);
        save();
        return subtask;
    }

    @Override
    public Task createNewTask(String inputName, String inputDescription, long id) throws ManagerSaveException {
        Task task = super.createNewTask(inputName, inputDescription, id);
        save();
        return task;
    }

    @Override
    public Epic createNewEpic(String inputName, String inputDescription, long id) throws ManagerSaveException {
        Epic epic = super.createNewEpic(inputName, inputDescription, id);
        save();
        return epic;
    }

    @Override
    public Subtask createNewSubtask(String inputName, String inputDescription, long id, String startTime, int duration, Epic epic) throws ManagerSaveException {
        Subtask subtask = super.createNewSubtask(inputName, inputDescription, id, startTime, duration, epic);
        save();
        return subtask;
    }

    @Override
    public Task createNewTask(String inputName, String inputDescription, long id, String startTime, int duration) throws ManagerSaveException {
        Task task = super.createNewTask(inputName, inputDescription, id, startTime, duration);
        save();
        return task;
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    @Override
    public void updateTask(long inputId, Task task) throws ManagerSaveException {
        super.updateTask(inputId, task);
        save();
    }

    @Override
    public void updateSubtask(long inputId, Subtask subtask) throws ManagerSaveException {
        super.updateSubtask(inputId, subtask);
        save();
    }

    @Override
    public void updateEpic(long inputId, Epic epic) throws ManagerSaveException {
        super.updateEpic(inputId, epic);
        save();
    }

    @Override
    public Task getTask(long inputId) throws ManagerSaveException {
        Task task = super.getTask(inputId);
        save();
        return task;
    }

    @Override
    public Subtask getSubtask(long inputId) throws ManagerSaveException {
        Subtask subtask = super.getSubtask(inputId);
        save();
        return subtask;
    }

    @Override
    public Epic getEpic(long inputId) throws ManagerSaveException {
        Epic epic = super.getEpic(inputId);
        save();
        return epic;
    }

    @Override
    public void removeTask(long inputId) throws ManagerSaveException {
        super.removeTask(inputId);
        save();
    }

    @Override
    public void removeAllTasks(HashMap<Long, Task> tasks) throws ManagerSaveException {
        super.removeAllTasks(tasks);
        save();
    }

    @Override
    public void removeAllSubtasks(HashMap<Long, Subtask> subtasks) throws ManagerSaveException {
        super.removeAllSubtasks(subtasks);
        save();
    }

    @Override
    public void removeAllEpics(HashMap<Long, Epic> epics) throws ManagerSaveException {
        super.removeAllEpics(epics);
        save();
    }

    @Override
    public void removeEpic(long inputId) throws ManagerSaveException {
        super.removeEpic(inputId);
        save();
    }

    @Override
    public void removeSubtask(long inputId) throws ManagerSaveException {
        super.removeSubtask(inputId);
        save();
    }
}
