package main;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.source.tree.ReturnTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest extends TaskManagerTest {
    Gson gson;

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
                Epic epic = server.fileBacked.getEpic(jsonObject.get("epic").getAsLong());
                server.fileBacked.getHistoryManager().remove(epic.getId());
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

    @BeforeEach
    void started() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Task.class, new TaskAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .create();
    }

    /*@Test
    void start() {
    }

    @Test
    void backup() {
    }*/

    @Test
    void shouldReturnTasks() throws ManagerSaveException, IOException, InterruptedException {
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Task task2 = server.fileBacked.createNewTask("Задача2", "Описание2", 2);
        Task task3 = server.fileBacked.createNewTask("Задача3", "Описание3", 3);
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.client.load("tasks"),
                                    new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера");
    }

    @Test
    void shouldReturnTaskById() {

    }

    @Test
    void shouldPostNewTask() {

    }

    @Test
    void shouldUpdateTaskById() {

    }

    @Test
    void shouldDeleteTasks() {

    }

    @Test
    void shouldDeleteTaskById() {

    }

    @Test
    void shouldReturnHistory() {

    }

    @Test
    void shouldReturnPrioritizedTasks() {

    }
}