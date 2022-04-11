package main;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;

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
            Subtask subtask = null;
            try {
                Epic epic = server.fileBacked.getEpic(jsonObject.get("epicId").getAsLong());
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

    @BeforeEach
    void started() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Task.class, new TaskAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .create();
    }

    @Test
    void shouldReturnTasksWhenGetRequest() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        server.fileBacked.createNewTask("Задача2", "Описание2", 2,
                                "11.12.2013", 3);
        String json = gson.toJson(server.fileBacked.getTasks());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals(json, response.body(), "HashMap tasks в виде Json не совпадает с Json ответом сервера.");
    }

    @Test
    void shouldNotReturnTasksWhenGetRequestAndTasksIsEmpty() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        assertEquals(0, server.fileBacked.getTasks().size(), "Размер HashMap должен быть равен 0.");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Задач нет.", response.body(),
                     "HashMap tasks в виде Json не совпадает с Json ответом сервера.");
    }

    /*@Test
    void shouldNotReturnTasksWhenPutRequest() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        server.fileBacked.createNewTask("Задача2", "Описание2", 2,
                "11.12.2013", 3);
        String json = gson.toJson(server.fileBacked.getTasks());
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Некорректный метод PUT", response.body(),
                "HashMap tasks в виде Json не совпадает с Json ответом сервера.");
    }*/

    /*@Test
    void shouldNotReturnTasksWhenWrongUri() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        server.fileBacked.createNewTask("Задача2", "Описание2", 2,
                "11.12.2013", 3);
        String json = gson.toJson(server.fileBacked.getTasks());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertNotEquals(json, response.body(), "Ожидался GET запрос.");
    }*/

    @Test
    void shouldReturnTaskByIdWhenGetRequest() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=2");
        server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        server.fileBacked.createNewTask("Задача2", "Описание2", 2,
                "11.12.2013", 3);
        String json = gson.toJson(server.fileBacked.getTask(2));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals(json, response.body(), "Задача в виде Json не совпадает с Json ответом сервера.");
    }

    @Test
    void shouldNotReturnTaskByIdWhenGetRequestAndTaskIsMissing() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=3");
        server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        server.fileBacked.createNewTask("Задача2", "Описание2", 2,
                "11.12.2013", 3);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Задачи 3 нет.", response.body(),"Задачи 3 не должно быть.");
    }

    @Test
    void shouldAddNewTaskWhenPostRequest() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        new Task("Задача1", "Описание1", 1);
        Task task = new Task("Задача2", "Описание2", 2,
                "11.12.2013", 3);
        assertEquals(0, server.fileBacked.getTasks().size(), "Размер HashMap должен быть равен 0.");
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Задача добавлена.", response.body(),
             "Задача 2 должна быть добавлена.");
        assertEquals(1, server.fileBacked.getTasks().size(), "Размер HashMap должен быть равен 1.");
        assertEquals(task, server.fileBacked.getTasks().get(2L), "Задачи не равны.");
    }

    @Test
    void shouldUpdateTaskWhenPostRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        assertEquals(0, server.fileBacked.getTasks().size(), "Размер HashMap должен быть равен 0.");
        server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Task task = new Task("Задача2", "Описание2", 2,
                "11.12.2013", 3);
        assertEquals(1, server.fileBacked.getTasks().size(), "Размер HashMap должен быть равен 1.");
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(1, server.fileBacked.getTasks().size(), "Размер HashMap должен быть равен 1.");
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Задача 1 успешно обновлена.", response.body(), "Задача 1 должна быть обновлена.");
        Task task1 = server.fileBacked.getTask(1);
        assertEquals(task.getName(), task1.getName(), "Поля названия не совпадают.");
        assertEquals(task.getDescription(), task1.getDescription(), "Поля описания не совпадают.");
        assertNotEquals(task.getId(), task1.getId(), "Id совпадают.");
        assertEquals(1, task1.getId(), "Id не совпадают.");
        assertEquals(task.getStartTime(), task1.getStartTime(), "Поля времени начала не совпадают.");
        assertEquals(task.getDuration(), task1.getDuration(), "Поля продолжительности не совпадают.");
    }

    @Test
    void shouldRemoveTaskByIdWhenDeleteRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=2");
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Task task2 = server.fileBacked.createNewTask("Задача2", "Описание2", 2,
                "11.12.2013", 3);
        assertEquals(2, server.fileBacked.getTasks().size(), "Размер HashMap должен быть равен 2.");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Задача 2 успешно удалена.", response.body(), "Задача 2 должна быть удалена.");
        assertEquals(1, server.fileBacked.getTasks().size(), "Размер HashMap должен быть равен 1.");
        assertFalse(server.fileBacked.getTasks().containsValue(task2), "HashMap не должна содержать задачу2.");
    }

    @Test
    void shouldRemoveTasksWhenDeleteRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Task task2 = server.fileBacked.createNewTask("Задача2", "Описание2", 2,
                "11.12.2013", 3);
        assertEquals(2, server.fileBacked.getTasks().size(), "Размер HashMap должен быть равен 2.");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Задачи удалены.", response.body(), "Задачи должны быть удалены.");
        assertEquals(0, server.fileBacked.getTasks().size(), "Размер HashMap должен быть равен 0.");
        assertFalse(server.fileBacked.getTasks().containsValue(task1), "HashMap не должна содержать задачу1.");
        assertFalse(server.fileBacked.getTasks().containsValue(task2), "HashMap не должна содержать задачу2.");
    }

    @Test
    void shouldReturnEpicsWhenGetRequest() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 4);
        String json = gson.toJson(server.fileBacked.getEpics());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals(json, response.body(), "HashMap epics в виде Json не совпадает с Json ответом сервера.");
    }

    @Test
    void shouldNotReturnEpicsWhenGetRequestAndEpicsIsEmpty() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        assertEquals(0, server.fileBacked.getEpics().size(), "Размер HashMap должен быть равен 0.");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Эпиков нет.", response.body(), "HashMap tasks в виде Json не совпадает с Json ответом сервера.");
    }

    @Test
    void shouldReturnEpicByIdWhenGetRequest() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=3");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 4);
        String json = gson.toJson(server.fileBacked.getEpic(3));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals(json, response.body(), "Эпик в виде Json не совпадает с Json ответом сервера.");
    }

    /*@Test
    void shouldNotReturnEpicByIdWhenGetRequestAndEpicIsMissing() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/epic/?id=1");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 4);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Эпика 1 нет.", response.body(),"Эпика 1 не должно быть.");
    }*/

    @Test
    void shouldAddNewEpicWhenPostRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        Epic epic = new Epic("Эпик1", "ОписаниеЭпика1", 1);
        assertEquals(0, server.fileBacked.getEpics().size(), "Размер HashMap должен быть равен 0.");
        String json1 = gson.toJson(epic);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json1))
                               .build();
        HttpResponse<String> response1 = server.fileBacked.getClient().getClient().send(request1,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response1.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Эпик добавлен.", response1.body(),
                "Задача 1 должна быть добавлена.");
        assertEquals(1, server.fileBacked.getEpics().size(), "Размер HashMap должен быть равен 1.");
        assertEquals(epic, server.fileBacked.getEpic(1L), "Эпики не равны.");
    }

    @Test
    void shouldUpdateEpicWhenPostRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=3");
        assertEquals(0, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 0.");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        Epic epic1 = new Epic("Эпик2", "ОписаниеЭпика2" , 2);
        String subtaskList1 = gson.toJson(epic.getSubtaskList());
        assertEquals(1, server.fileBacked.getEpics().size(), "Размер HashMap должен быть равен 1.");
        String json = gson.toJson(epic1);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(1, server.fileBacked.getEpics().size(), "Размер HashMap должен быть равен 1.");
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Эпик 3 успешно обновлен.", response.body(), "Эпик 3 должен быть обновлен.");
        Epic epic2 = server.fileBacked.getEpic(3L);
        String subtaskList2 = gson.toJson(epic2.getSubtaskList());
        assertEquals(epic1.getName(), epic2.getName(), "Поля названия не совпадают.");
        assertEquals(epic1.getDescription(), epic2.getDescription(), "Поля описания не совпадают.");
        assertNotEquals(epic1.getId(), epic2.getId(), "Id совпадают.");
        assertEquals(subtaskList1, subtaskList2, "Список подзадач должен быть одинаковым.");
    }

    @Test
    void shouldRemoveEpicByIdWhenDeleteRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=3");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        Subtask subtask1 = server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        Subtask subtask2 = server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        Epic epic1 = server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2" , 4);
        assertEquals(2, server.fileBacked.getEpics().size(), "Размер HashMap должен быть равен 2.");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Эпик 3 успешно удалён.", response.body(),
                "Эпик 3 должен быть удален.");
        assertEquals(1, server.fileBacked.getEpics().size(), "Размер HashMap должен быть равен 1.");
        assertFalse(server.fileBacked.getSubtasks().containsValue(subtask1),
                "HashMap не должна содержать подзадачу1.");
        assertFalse(server.fileBacked.getSubtasks().containsValue(subtask2),
                "HashMap не должна содержать подзадачу2.");
    }

    @Test
    void shouldRemoveEpicsWhenDeleteRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2" , 4);
        assertEquals(2, server.fileBacked.getEpics().size(), "Размер HashMap должен быть равен 2.");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Эпики удалены.", response.body(), "Эпики должны быть удалены.");
        assertEquals(0, server.fileBacked.getEpics().size(), "Размер HashMap должен быть равен 0.");
        assertEquals(0, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 0.");
    }

    @Test
    void shouldReturnSubtasksWhenGetRequest() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        String json = gson.toJson(server.fileBacked.getSubtasks());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals(json, response.body(), "HashMap subtasks в виде Json не совпадает с Json ответом сервера.");
    }

    @Test
    void shouldNotReturnSubtasksWhenGetRequestAndSubtasksIsEmpty() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        assertEquals(0, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 0.");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Подзадач нет.", response.body(),
                "HashMap tasks в виде Json не совпадает с Json ответом сервера.");
    }

    @Test
    void shouldReturnSubtaskByIdWhenGetRequest() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=2");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        String json = gson.toJson(server.fileBacked.getSubtask(2));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals(json, response.body(), "Подзадача в виде Json не совпадает с Json ответом сервера.");
    }

    /*@Test
    void shouldNotReturnSubtaskByIdWhenGetRequestAndSubtaskIsMissing() throws ManagerSaveException, IOException,
                                                                              InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks/subtask/?id=3");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Подзадачи 3 нет.", response.body(),"Подзадачи 3 не должно быть.");
    }*/

    @Test
    void shouldAddNewSubtaskWhenPostRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        Subtask subtask1 = new Subtask("Задача1", "Описание1", 1, epic);
        Subtask subtask2 = new Subtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        assertEquals(0, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 0.");
        String json1 = gson.toJson(subtask1);
        String json2 = gson.toJson(subtask2);
        HttpRequest request1 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json1))
                .build();
        HttpResponse<String> response1 = server.fileBacked.getClient().getClient().send(request1,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response1.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Задача добавлена.", response1.body(),
                "Задача 1 должна быть добавлена.");
        assertEquals(1, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 1.");
        assertEquals(subtask1, server.fileBacked.getSubtasks().get(1L), "Задачи не равны.");
        HttpRequest request2 = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json2))
                .build();
        HttpResponse<String> response2 = server.fileBacked.getClient().getClient().send(request2,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response2.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Задача добавлена.", response2.body(),
                "Задача 2 должна быть добавлена.");
        assertEquals(2, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 2.");
        assertEquals(subtask2, server.fileBacked.getSubtasks().get(2L), "Задачи не равны.");
    }

    @Test
    void shouldUpdateSubtaskWhenPostRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=1");
        assertEquals(0, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 0.");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        Subtask subtask2 = new Subtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        assertEquals(1, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 1.");
        String json = gson.toJson(subtask2);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(1, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 1.");
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Подзадача 1 успешно обновлена.", response.body(), "Подзадача 1 должна быть обновлена.");
        Subtask subtask3 = server.fileBacked.getSubtask(1L);
        assertEquals(subtask2.getName(), subtask3.getName(), "Поля названия не совпадают.");
        assertEquals(subtask2.getDescription(), subtask3.getDescription(), "Поля описания не совпадают.");
        assertNotEquals(subtask2.getId(), subtask3.getId(), "Id совпадают.");
        assertEquals(1, subtask3.getId(), "Id не совпадают.");
        assertEquals(subtask2.getStartTime(), subtask3.getStartTime(), "Поля времени начала не совпадают.");
        assertEquals(subtask2.getDuration(), subtask3.getDuration(), "Поля продолжительности не совпадают.");
    }

    @Test
    void shouldRemoveSubtaskByIdWhenDeleteRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=2");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        Subtask subtask2 = server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        assertEquals(2, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 2.");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Подзадача 2 успешно удалена.", response.body(),
                "Подзадача 2 должна быть удалена.");
        assertEquals(1, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 1.");
        assertFalse(server.fileBacked.getTasks().containsValue(subtask2),
                "HashMap не должна содержать подзадачу2.");
    }

    @Test
    void shouldRemoveSubtasksWhenDeleteRequest() throws IOException, InterruptedException, ManagerSaveException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        Subtask subtask1 = server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        Subtask subtask2 = server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        assertEquals(2, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 2.");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Код статуса сервера не совпадает с ожидаемым." );
        assertEquals("Подзадачи удалены.", response.body(), "Подзадачи должны быть удалены.");
        assertEquals(0, server.fileBacked.getSubtasks().size(), "Размер HashMap должен быть равен 0.");
        assertFalse(server.fileBacked.getSubtasks().containsValue(subtask1),
                "HashMap не должна содержать подзадачу1.");
        assertFalse(server.fileBacked.getSubtasks().containsValue(subtask2),
                "HashMap не должна содержать подзадачу2.");
    }

    @Test
    void shouldNotReturnSubtaskListWhenEpicsNotContainsEpic() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=5");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals("Подзадач нет.", response.body(), "SubtaskList должен быть пустым.");
    }

    /*@Test
    void shouldReturnNullWhenEpicsNotContainsSubtasks() {

    }*/

    @Test
    void shouldReturnSubtaskListByEpicId() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=5");
        Epic epic1 = server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 5);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic1);
        String json = gson.toJson(epic1.getSubtaskList());
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(json, response.body(), "SubtaskList в виде Json не совпадает с Json ответом сервера.");
    }

    @Test
    void shouldReturnHistoryWhenGetRequest() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/history");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        server.fileBacked.createNewTask("Задача4", "Описание4", 4,
                "11.12.2015", 3);
        server.fileBacked.getTask(4L);
        server.fileBacked.getEpic(3L);
        server.fileBacked.getSubtask(2L);
        server.fileBacked.getSubtask(1L);
        List<Task> history = server.fileBacked.getHistory();
        String jsonHistory = gson.toJson(history);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(jsonHistory, response.body(), "history в виде Json не совпадает с Json ответом сервера.");
    }

    @Test
    void shouldReturnPrioritizedTasksWhenGetRequest() throws ManagerSaveException, IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 3);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2,
                "11.12.2013", 3, epic);
        server.fileBacked.createNewTask("Задача4", "Описание4", 4,
                "11.12.2015", 3);
        Set<Task> sortedTasks = server.fileBacked.getPrioritizedTasks();
        String json = gson.toJson(sortedTasks);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = server.fileBacked.getClient().getClient().send(request,
                HttpResponse.BodyHandlers.ofString());
        assertEquals(json, response.body(), "sortedTasks в виде Json не совпадает с Json ответом сервера.");
    }

    /*@Test
    void shouldReturnNullWhenTasksIsEmpty() throws IOException, InterruptedException {
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertNull(tasks, "Список должен быть равен null.");
    }

    @Test
    void shouldReturnTasks() throws ManagerSaveException, IOException, InterruptedException {
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Task task2 = server.fileBacked.createNewTask("Задача2", "Описание2", 2);
        Task task3 = server.fileBacked.createNewTask("Задача3", "Описание3", 3);
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                                    new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера");
    }

    @Test
    void shouldReturnNullWhenTasksNotContainTask() throws ManagerSaveException, IOException, InterruptedException {
        server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        server.fileBacked.createNewTask("Задача2", "Описание2", 2);
        server.fileBacked.createNewTask("Задача3", "Описание3", 3);
        server.fileBacked.createNewEpic("Задача3", "Описание3", 4);
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertNull(tasks.get(4L), "Задачи с id 4 не должно быть на сервере.");
    }

    @Test
    void shouldReturnTaskById() throws ManagerSaveException, IOException, InterruptedException {
        server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        server.fileBacked.createNewTask("Задача2", "Описание2", 2);
        server.fileBacked.createNewTask("Задача3", "Описание3", 3);
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                                                  new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(server.fileBacked.getTask(1L), tasks.get(1L),
                "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(server.fileBacked.getTask(2L), tasks.get(2L),
                "Задача 2 не равна задаче 2 при загрузке с сервера");
        assertEquals(server.fileBacked.getTask(3L), tasks.get(3L),
                "Задача 3 не равна задаче 3 при загрузке с сервера");
    }

    @Test
    void shouldUpdateTaskById() throws ManagerSaveException, IOException, InterruptedException {
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Task task2 = server.fileBacked.createNewTask("Задача2", "Описание2", 2);
        Task task3 = server.fileBacked.createNewTask("Задача3", "Описание3", 3);
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                                                  new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера");
        Task task4 = new Task("Задача4", "Описание4", 4);
        Task task5 = new Task("Задача5", "Описание5", 5);
        Task task6 = new Task("Задача6", "Описание6", 6);
        server.fileBacked.updateTask(1L, task4);
        server.fileBacked.updateTask(2L, task5);
        server.fileBacked.updateTask(3L, task6);
        HashMap<Long, Task> tasks1 = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task4.getName(), tasks1.get(1L).getName(),
                "Название задачи 4 не совпадает с названием обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getDescription(), tasks1.get(1L).getDescription(),
                "Описание задачи 4 не совпадает с описанием обновляемой задачи 1 при загрузке с сервера");
        assertNotEquals(task4.getId(), tasks1.get(1L).getId(),
                "Id задачи 4 совпадает с Id обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getStartTime(), tasks1.get(1L).getStartTime(),
                "Время начала задачи 4 не совпадает с временем обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getDuration(), tasks1.get(1L).getDuration(),
                "Время выполнения задачи 4 не совпадает с временем обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task5.getName(), tasks1.get(2L).getName(),
                "Название задачи 5 не совпадает с названием обновляемой задачи 2 при загрузке с сервера");
        assertEquals(task5.getDescription(), tasks1.get(2L).getDescription(),
                "Описание задачи 5 не совпадает с описанием обновляемой задачи 2 при загрузке с сервера");
        assertNotEquals(task5.getId(), tasks1.get(2L).getId(),
                "Id задачи 5 совпадает с Id обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task5.getStartTime(), tasks1.get(2L).getStartTime(),
                "Время начала задачи 5 не совпадает с временем обновляемой задачи 2 при загрузке с сервера");
        assertEquals(task5.getDuration(), tasks1.get(2L).getDuration(),
                "Время выполнения задачи 5 не совпадает с временем обновляемой задачи 2 при загрузке с сервера");
        assertEquals(task6.getName(), tasks1.get(3L).getName(),
                "Название задачи 6 не совпадает с названием обновляемой задачи 3 при загрузке с сервера");
        assertEquals(task6.getDescription(), tasks1.get(3L).getDescription(),
                "Описание задачи 6 не совпадает с описанием обновляемой задачи 3 при загрузке с сервера");
        assertNotEquals(task6.getId(), tasks1.get(3L).getId(),
                "Id задачи 6 совпадает с Id обновляемой задачи 3 при загрузке с сервера");
        assertEquals(task6.getStartTime(), tasks1.get(3L).getStartTime(),
                "Время начала задачи 6 не совпадает с временем обновляемой задачи 3 при загрузке с сервера");
        assertEquals(task6.getDuration(), tasks1.get(3L).getDuration(),
                "Время выполнения задачи 6 не совпадает с временем обновляемой задачи 3 при загрузке с сервера");
    }

    @Test
    void shouldNotUpdateTaskWhenTasksNotContainTask() throws ManagerSaveException, IOException, InterruptedException {
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(1, tasks.size(), "Размер HashMap должен быть равен 1.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        Task task4 = new Task("Задача4", "Описание4", 4);
        server.fileBacked.updateTask(5L, task4);
        HashMap<Long, Task> tasks1 = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(1, tasks.size(), "Размер HashMap должен быть равен 1.");
        assertEquals(tasks1.get(1L), task1, "Задача 1 не совпадает с задачей 1 при загрузке с сервера.");
        assertNotEquals(task4.getName(), tasks1.get(1L).getName(),
                "Название задачи 4 совпадает с названием обновляемой задачи 1 при загрузке с сервера.");
        assertNotEquals(task4.getDescription(), tasks1.get(1L).getDescription(),
                "Описание задачи 4 совпадает с описанием обновляемой задачи 1 при загрузке с сервера.");
        assertNotEquals(task4.getId(), tasks1.get(1L).getId(),
                "Id задачи 4 совпадает с Id обновляемой задачи 1 при загрузке с сервера.");
    }

    @Test
    void shouldDeleteAllTasks() throws ManagerSaveException, IOException, InterruptedException {
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Task task2 = server.fileBacked.createNewTask("Задача2", "Описание2", 2);
        Task task3 = server.fileBacked.createNewTask("Задача3", "Описание3", 3);
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера");
        server.fileBacked.removeAllTasks(server.fileBacked.getTasks());
        HashMap<Long, Task> tasks1 = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(0, tasks1.size(), "Размер HashMap должен быть равен 0.");
    }

    @Test
    void shouldDoNothingWhenDeleteEmptyTasks() throws ManagerSaveException, IOException, InterruptedException {
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertNull(tasks, "Список должен быть равен null.");
        server.fileBacked.removeAllTasks(server.fileBacked.getTasks());
        HashMap<Long, Task> tasks1 = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(0, tasks1.size(), "HashMap должна быть пустой.");
    }

    @Test
    void shouldDeleteTaskById() throws ManagerSaveException, IOException, InterruptedException {
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Task task2 = server.fileBacked.createNewTask("Задача2", "Описание2", 2);
        Task task3 = server.fileBacked.createNewTask("Задача3", "Описание3", 3);
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера.");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера.");
        server.fileBacked.removeTask(2L);
        HashMap<Long, Task> tasks1 = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(2, tasks1.size(), "Размер HashMap должен быть равен 2.");
        assertFalse(tasks1.containsValue(task2), "HashMap не должна содержать задачу2.");
    }

    @Test
    void shouldNotDeleteTaskWhenTasksNotContainTask() throws ManagerSaveException, IOException, InterruptedException {
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Task task2 = server.fileBacked.createNewTask("Задача2", "Описание2", 2);
        Task task3 = server.fileBacked.createNewTask("Задача3", "Описание3", 3);
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера.");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера.");
        server.fileBacked.removeTask(5L);
        HashMap<Long, Task> tasks1 = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(3, tasks1.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера.");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера.");
    }

    @Test
    void shouldNotNothingWhenDeleteTaskByIdFromEmptyTasks() throws ManagerSaveException, IOException,
                                                                   InterruptedException {
        HashMap<Long, Task> tasks = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertNull(tasks, "Список должен быть равен null.");
        server.fileBacked.removeTask(1L);
        HashMap<Long, Task> tasks1 = gson.fromJson(server.fileBacked.load("tasks"),
                new TypeToken<HashMap<Long, Task>>() {}.getType());
        assertEquals(0, tasks1.size(), "HashMap должна быть пустой.");
    }

    @Test
    void shouldReturnNullWhenSubtasksIsEmpty() throws IOException, InterruptedException {
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertNull(tasks, "Список должен быть равен null.");
    }

    @Test
    void shouldReturnSubtasks() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        Subtask task1 = server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        Subtask task2 = server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        Subtask task3 = server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера");
    } //здесь же проверяется POST запрос для сабтаски/ок, то есть её/их добавление на сервер

    @Test
    void shouldReturnNullWhenSubtasksNotContainSubtask() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertNull(tasks.get(5L), "Задачи с id 5 не должно быть на сервере.");
    }

    @Test
    void shouldReturnSubtaskById() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(server.fileBacked.getSubtask(1L), tasks.get(1L),
                "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(server.fileBacked.getSubtask(2L), tasks.get(2L),
                "Задача 2 не равна задаче 2 при загрузке с сервера");
        assertEquals(server.fileBacked.getSubtask(3L), tasks.get(3L),
                "Задача 3 не равна задаче 3 при загрузке с сервера");
    }

    @Test
    void shouldUpdateSubtaskById() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        Subtask task1 = server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        Subtask task2 = server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        Subtask task3 = server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера");
        Subtask task4 = server.fileBacked.createNewSubtask("Задача4", "Описание4", 4, epic);
        Subtask task5 = server.fileBacked.createNewSubtask("Задача5", "Описание5", 5, epic);
        Subtask task6 = server.fileBacked.createNewSubtask("Задача6", "Описание6", 6, epic);
        server.fileBacked.updateSubtask(1L, task4);
        server.fileBacked.updateSubtask(2L, task5);
        server.fileBacked.updateSubtask(3L, task6);
        HashMap<Long, Subtask> tasks1 = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task4.getName(), tasks1.get(1L).getName(),
                "Название задачи 4 не совпадает с названием обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getDescription(), tasks1.get(1L).getDescription(),
                "Описание задачи 4 не совпадает с описанием обновляемой задачи 1 при загрузке с сервера");
        assertNotEquals(task4.getId(), tasks1.get(1L).getId(),
                "Id задачи 4 совпадает с Id обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getStartTime(), tasks1.get(1L).getStartTime(),
                "Время начала задачи 4 не совпадает с временем обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getDuration(), tasks1.get(1L).getDuration(),
                "Время выполнения задачи 4 не совпадает с временем обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task5.getName(), tasks1.get(2L).getName(),
                "Название задачи 5 не совпадает с названием обновляемой задачи 2 при загрузке с сервера");
        assertEquals(task5.getDescription(), tasks1.get(2L).getDescription(),
                "Описание задачи 5 не совпадает с описанием обновляемой задачи 2 при загрузке с сервера");
        assertNotEquals(task5.getId(), tasks1.get(2L).getId(),
                "Id задачи 5 совпадает с Id обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task5.getStartTime(), tasks1.get(2L).getStartTime(),
                "Время начала задачи 5 не совпадает с временем обновляемой задачи 2 при загрузке с сервера");
        assertEquals(task5.getDuration(), tasks1.get(2L).getDuration(),
                "Время выполнения задачи 5 не совпадает с временем обновляемой задачи 2 при загрузке с сервера");
        assertEquals(task6.getName(), tasks1.get(3L).getName(),
                "Название задачи 6 не совпадает с названием обновляемой задачи 3 при загрузке с сервера");
        assertEquals(task6.getDescription(), tasks1.get(3L).getDescription(),
                "Описание задачи 6 не совпадает с описанием обновляемой задачи 3 при загрузке с сервера");
        assertNotEquals(task6.getId(), tasks1.get(3L).getId(),
                "Id задачи 6 совпадает с Id обновляемой задачи 3 при загрузке с сервера");
        assertEquals(task6.getStartTime(), tasks1.get(3L).getStartTime(),
                "Время начала задачи 6 не совпадает с временем обновляемой задачи 3 при загрузке с сервера");
        assertEquals(task6.getDuration(), tasks1.get(3L).getDuration(),
                "Время выполнения задачи 6 не совпадает с временем обновляемой задачи 3 при загрузке с сервера");
    }

    @Test
    void shouldNotUpdateSubtaskWhenSubtasksNotContainSubtask() throws ManagerSaveException, IOException,
                                                                      InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 2);
        Subtask task1 = server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(1, tasks.size(), "Размер HashMap должен быть равен 1.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        Subtask task4 = new Subtask("Задача4", "Описание4", 4, epic);
        server.fileBacked.updateSubtask(5L, task4);
        HashMap<Long, Subtask> tasks1 = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(1, tasks.size(), "Размер HashMap должен быть равен 1.");
        assertEquals(tasks1.get(1L), task1, "Задача 1 не совпадает с задачей 1 при загрузке с сервера.");
        assertNotEquals(task4.getName(), tasks1.get(1L).getName(),
                "Название задачи 4 совпадает с названием обновляемой задачи 1 при загрузке с сервера.");
        assertNotEquals(task4.getDescription(), tasks1.get(1L).getDescription(),
                "Описание задачи 4 совпадает с описанием обновляемой задачи 1 при загрузке с сервера.");
        assertNotEquals(task4.getId(), tasks1.get(1L).getId(),
                "Id задачи 4 совпадает с Id обновляемой задачи 1 при загрузке с сервера.");
        assertEquals(task4.getStartTime(), tasks1.get(1L).getStartTime(),
                "Время начала задачи 4 не совпадает с временем обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getDuration(), tasks1.get(1L).getDuration(),
                "Время выполнения задачи 4 не совпадает с временем обновляемой задачи 1 при загрузке с сервера");
    }

    @Test
    void shouldDeleteAllSubtasks() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        Subtask task1 = server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        Subtask task2 = server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        Subtask task3 = server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера");
        server.fileBacked.removeAllSubtasks(server.fileBacked.getSubtasks());
        HashMap<Long, Subtask> tasks1 = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(0, tasks1.size(), "Размер HashMap должен быть равен 0.");
    }

    @Test
    void shouldDoNothingWhenDeleteEmptySubtasks() throws ManagerSaveException, IOException, InterruptedException {
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertNull(tasks, "Список должен быть равен null.");
        server.fileBacked.removeAllSubtasks(server.fileBacked.getSubtasks());
        HashMap<Long, Subtask> tasks1 = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(0, tasks1.size(), "HashMap должна быть пустой.");
    }

    @Test
    void shouldDeleteSubtaskById() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        Subtask task1 = server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        Subtask task2 = server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        Subtask task3 = server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера.");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера.");
        server.fileBacked.removeSubtask(2L);
        HashMap<Long, Subtask> tasks1 = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(2, tasks1.size(), "Размер HashMap должен быть равен 2.");
        assertFalse(tasks1.containsValue(task2), "HashMap не должна содержать задачу2.");
    }

    @Test
    void shouldNotDeleteSubtaskWhenSubtasksNotContainSubtask() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        Subtask task1 = server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        Subtask task2 = server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        Subtask task3 = server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(3, tasks.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера.");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера.");
        server.fileBacked.removeSubtask(5L);
        HashMap<Long, Subtask> tasks1 = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(3, tasks1.size(), "Размер HashMap должен быть равен 3.");
        assertEquals(task1, tasks.get(1L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        assertEquals(task2, tasks.get(2L), "Задача 2 не равна задаче 2 при загрузке с сервера.");
        assertEquals(task3, tasks.get(3L), "Задача 3 не равна задаче 3 при загрузке с сервера.");
    }

    @Test
    void shouldNotNothingWhenDeleteSubtaskByIdFromEmptySubtasks() throws ManagerSaveException, IOException,
            InterruptedException {
        HashMap<Long, Subtask> tasks = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertNull(tasks, "Список должен быть равен null.");
        server.fileBacked.removeSubtask(1L);
        HashMap<Long, Subtask> tasks1 = gson.fromJson(server.fileBacked.load("subtasks"),
                new TypeToken<HashMap<Long, Subtask>>() {}.getType());
        assertEquals(0, tasks1.size(), "HashMap должна быть пустой.");
    }

    ///
    @Test
    void shouldReturnEpics() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        Epic epic1 = server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 5);
        HashMap<Long, Epic> tasks = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(2, tasks.size(), "Размер HashMap должен быть равен 2.");
        assertEquals(epic, tasks.get(4L), "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(epic1, tasks.get(5L), "Задача 2 не равна задаче 2 при загрузке с сервера");
    } //здесь же проверяется POST запрос для сабтаски/ок, то есть её/их добавление на сервер

    @Test
    void shouldReturnNullWhenEpicsNotContainEpic() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 5);
        HashMap<Long, Epic> tasks = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(2, tasks.size(), "Размер HashMap должен быть равен 2.");
        assertNull(tasks.get(6L), "Задачи с id 6 не должно быть на сервере.");
    }

    @Test
    void shouldReturnEpicById() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 5);
        HashMap<Long, Epic> tasks = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(2, tasks.size(), "Размер HashMap должен быть равен 2.");
        assertEquals(server.fileBacked.getEpic(4L), tasks.get(4L), "Эпик 4 не равен эпику 4 при загрузке с сервера");
        assertEquals(server.fileBacked.getEpic(5L), tasks.get(5L), "Эпик 5 не равен эпику 5 при загрузке с сервера");
    }

    @Test
    void shouldUpdateEpicById() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        Epic epic1 = server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 5);
        HashMap<Long, Epic> tasks = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(2, tasks.size(), "Размер HashMap должен быть равен 2.");
        assertEquals(epic, tasks.get(4L), "Задача 1 не равна задаче 1 при загрузке с сервера");
        assertEquals(epic1, tasks.get(5L), "Задача 2 не равна задаче 2 при загрузке с сервера");
        Epic task4 = server.fileBacked.createNewEpic("Задача4", "Описание4", 6);
        Epic task5 = server.fileBacked.createNewEpic("Задача5", "Описание5", 7);
        server.fileBacked.updateEpic(4L, task4);
        server.fileBacked.updateEpic(5L, task5);
        HashMap<Long, Epic> tasks1 = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(2, tasks.size(), "Размер HashMap должен быть равен 2.");
        assertEquals(task4.getName(), tasks1.get(4L).getName(),
                "Название задачи 4 не совпадает с названием обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getDescription(), tasks1.get(4L).getDescription(),
                "Описание задачи 4 не совпадает с описанием обновляемой задачи 1 при загрузке с сервера");
        assertNotEquals(task4.getId(), tasks1.get(4L).getId(),
                "Id задачи 4 совпадает с Id обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getStartTime(), tasks1.get(4L).getStartTime(),
                "Время начала задачи 4 не совпадает с временем обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getDuration(), tasks1.get(4L).getDuration(),
                "Время выполнения задачи 4 не совпадает с временем обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task5.getName(), tasks1.get(5L).getName(),
                "Название задачи 5 не совпадает с названием обновляемой задачи 2 при загрузке с сервера");
        assertEquals(task5.getDescription(), tasks1.get(5L).getDescription(),
                "Описание задачи 5 не совпадает с описанием обновляемой задачи 2 при загрузке с сервера");
        assertNotEquals(task5.getId(), tasks1.get(5L).getId(),
                "Id задачи 5 совпадает с Id обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task5.getStartTime(), tasks1.get(5L).getStartTime(),
                "Время начала задачи 5 не совпадает с временем обновляемой задачи 2 при загрузке с сервера");
        assertEquals(task5.getDuration(), tasks1.get(5L).getDuration(),
                "Время выполнения задачи 5 не совпадает с временем обновляемой задачи 2 при загрузке с сервера");
    }

    @Test
    void shouldNotUpdateEpicWhenEpicsNotContainEpic() throws ManagerSaveException, IOException,
            InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 2);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        HashMap<Long, Epic> tasks = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(1, tasks.size(), "Размер HashMap должен быть равен 1.");
        assertEquals(epic, tasks.get(2L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        Epic task4 = new Epic("Задача4", "Описание4", 4);
        server.fileBacked.updateEpic(5L, task4);
        HashMap<Long, Epic> tasks1 = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(1, tasks.size(), "Размер HashMap должен быть равен 1.");
        assertEquals(tasks1.get(2L), epic, "Эпик 2 не совпадает с эпиком 2 при загрузке с сервера.");
        assertNotEquals(task4.getName(), tasks1.get(2L).getName(),
                "Название задачи 2 совпадает с названием обновляемой задачи 1 при загрузке с сервера.");
        assertNotEquals(task4.getDescription(), tasks1.get(2L).getDescription(),
                "Описание задачи 2 совпадает с описанием обновляемой задачи 1 при загрузке с сервера.");
        assertNotEquals(task4.getId(), tasks1.get(2L).getId(),
                "Id задачи 2 совпадает с Id обновляемой задачи 1 при загрузке с сервера.");
        assertEquals(task4.getStartTime(), tasks1.get(2L).getStartTime(),
                "Время начала задачи 2 не совпадает с временем обновляемой задачи 1 при загрузке с сервера");
        assertEquals(task4.getDuration(), tasks1.get(2L).getDuration(),
                "Время выполнения задачи 2 не совпадает с временем обновляемой задачи 1 при загрузке с сервера");
    }

    @Test
    void shouldDeleteAllEpics() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        Epic epic1 = server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 5);
        HashMap<Long, Epic> tasks = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(2, tasks.size(), "Размер HashMap должен быть равен 2.");
        assertEquals(epic, tasks.get(4L), "Задача 4 не равна задаче 4 при загрузке с сервера");
        assertEquals(epic1, tasks.get(5L), "Задача 5 не равна задаче 5 при загрузке с сервера");
        server.fileBacked.removeAllSubtasks(server.fileBacked.getSubtasks());
        HashMap<Long, Epic> tasks1 = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(0, tasks1.size(), "Размер HashMap должен быть равен 0.");
    }

    @Test
    void shouldDoNothingWhenDeleteEmptyEpics() throws ManagerSaveException, IOException, InterruptedException {
        HashMap<Long, Epic> tasks = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertNull(tasks, "Список должен быть равен null.");
        server.fileBacked.removeAllSubtasks(server.fileBacked.getSubtasks());
        HashMap<Long, Epic> tasks1 = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(0, tasks1.size(), "HashMap должна быть пустой.");
    }

    @Test
    void shouldDeleteEpicById() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        Epic epic1 = server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 5);
        HashMap<Long, Epic> tasks = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(2, tasks.size(), "Размер HashMap должен быть равен 2.");
        assertEquals(epic, tasks.get(4L), "Задача 4 не равна задаче 4 при загрузке с сервера.");
        assertEquals(epic1, tasks.get(5L), "Задача 5 не равна задаче 5 при загрузке с сервера.");
        server.fileBacked.removeEpic(4L);
        HashMap<Long, Epic> tasks1 = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(1, tasks1.size(), "Размер HashMap должен быть равен 1.");
        assertFalse(tasks1.containsValue(epic), "HashMap не должна содержать эпик 4.");
    }

    @Test
    void shouldNotDeleteEpicWhenEpicsNotContainEpic() throws ManagerSaveException, IOException, InterruptedException {
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 4);
        server.fileBacked.createNewSubtask("Задача1", "Описание1", 1, epic);
        server.fileBacked.createNewSubtask("Задача2", "Описание2", 2, epic);
        server.fileBacked.createNewSubtask("Задача3", "Описание3", 3, epic);
        Epic epic1 = server.fileBacked.createNewEpic("Эпик2", "ОписаниеЭпика2", 5);
        HashMap<Long, Epic> tasks = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(2, tasks.size(), "Размер HashMap должен быть равен 2.");
        assertEquals(epic, tasks.get(4L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        assertEquals(epic1, tasks.get(5L), "Задача 2 не равна задаче 2 при загрузке с сервера.");
        server.fileBacked.removeEpic(6L);
        HashMap<Long, Epic> tasks1 = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(2, tasks1.size(), "Размер HashMap должен быть равен 2.");
        assertEquals(epic, tasks.get(4L), "Задача 1 не равна задаче 1 при загрузке с сервера.");
        assertEquals(epic1, tasks.get(5L), "Задача 2 не равна задаче 2 при загрузке с сервера.");
    }

    @Test
    void shouldNotNothingWhenDeleteEpicByIdFromEmptyEpics() throws ManagerSaveException, IOException,
            InterruptedException {
        HashMap<Long, Epic> tasks = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertNull(tasks, "Список должен быть равен null.");
        server.fileBacked.removeEpic(1L);
        HashMap<Long, Epic> tasks1 = gson.fromJson(server.fileBacked.load("epics"),
                new TypeToken<HashMap<Long, Epic>>() {}.getType());
        assertEquals(0, tasks1.size(), "HashMap должна быть пустой.");
    }

    @Test
    void shouldReturnNullWhenHistoryIsEmpty() throws IOException, InterruptedException {
        List<Long> history = gson.fromJson(server.fileBacked.load("history"),
                new TypeToken<ArrayList<Long>>() {}.getType());
        assertNull(history, "Список должен быть равен null.");
    }

    @Test
    void shouldReturnHistoryWhenHistoryContainsTask() throws IOException, InterruptedException, ManagerSaveException {
        List<Long> history = gson.fromJson(server.fileBacked.load("history"),
                new TypeToken<ArrayList<Long>>() {}.getType());
        assertNull(history, "Список должен быть равен null.");
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        server.fileBacked.getTask(1L);
        List<Long> history1 = gson.fromJson(server.fileBacked.load("history"),
                new TypeToken<ArrayList<Long>>() {}.getType());
        assertEquals(1, history1.size(), "Размер списка истории должен быть равен 1.");
        assertEquals(task1.getId(), history1.get(0), "Id задачи из списка истории не равен Id задачи1");
    }

    @Test
    void shouldReturnHistoryWhenHistoryContainsSubtask() throws IOException, InterruptedException,
                                                                ManagerSaveException {
        List<Long> history = gson.fromJson(server.fileBacked.load("history"),
                new TypeToken<ArrayList<Long>>() {}.getType());
        assertNull(history, "Список должен быть равен null.");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 2);
        Subtask task1 = server.fileBacked.createNewSubtask("Подзадача1", "Описание1", 1,
                                                           epic);
        server.fileBacked.getSubtask(1L);
        List<Long> history1 = gson.fromJson(server.fileBacked.load("history"),
                new TypeToken<ArrayList<Long>>() {}.getType());
        assertEquals(1, history1.size(), "Размер списка истории должен быть равен 1.");
        assertEquals(task1.getId(), history1.get(0),
                "Id подзадачи из списка истории не равен id подзадачи1");
    }

    @Test
    void shouldReturnHistoryWhenHistoryContainsEpic() throws IOException, InterruptedException, ManagerSaveException {
        List<Long> history = gson.fromJson(server.fileBacked.load("history"),
                new TypeToken<ArrayList<Long>>() {}.getType());
        assertNull(history, "Список должен быть равен null.");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 1);
        server.fileBacked.getEpic(1L);
        List<Long> history1 = gson.fromJson(server.fileBacked.load("history"),
                new TypeToken<ArrayList<Long>>() {}.getType());
        assertEquals(1, history1.size(), "Размер списка истории должен быть равен 1.");
        assertEquals(epic.getId(), history1.get(0),
                "Id эпика из списка истории не равен id эпика1");
    }

    @Test
    void shouldReturnHistory() throws ManagerSaveException, IOException, InterruptedException {
        List<Long> history = gson.fromJson(server.fileBacked.load("history"),
                new TypeToken<ArrayList<Long>>() {}.getType());
        assertNull(history, "Список должен быть равен null.");
        Task task = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 2);
        Subtask subtask = server.fileBacked.createNewSubtask("Подзадача1", "ОписаниеПодзадачи1",
                3, epic);
        server.fileBacked.getTask(1L);
        server.fileBacked.getEpic(2L);
        server.fileBacked.getSubtask(3L);
        List<Long> history1 = gson.fromJson(server.fileBacked.load("history"),
                new TypeToken<ArrayList<Long>>() {}.getType());
        assertEquals(3, history1.size(), "Размер списка истории должен быть равен 3.");
        assertEquals(task.getId(), history1.get(0), "Id задачи 1 не равен id задачи 1 при загрузке с сервера");
        assertEquals(epic.getId(), history1.get(1),"Id эпика 2 не равен Id эпика 2 при загрузке с сервера");
        assertEquals(subtask.getId(), history1.get(2),
                "Id подзадачи 3 не равен Id подзадачи 3 при загрузке с сервера");
    }

    @Test
    void shouldReturnNullWhenPrioritizedTasksIsEmpty() {
        assertEquals(0, server.fileBacked.sortedTasks.size(), "Размер списка должен быть равен 0.");
    }

    @Test
    void shouldReturnPrioritizedTasksWhenPrioritizedTasksContainsTask() throws ManagerSaveException {
        assertEquals(0, server.fileBacked.sortedTasks.size(), "Размер списка должен быть равен 0.");
        Task task1 = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        assertEquals(1, server.fileBacked.sortedTasks.size(), "Размер списка должен быть равен 1.");
        Iterator<Task> iterator =  server.fileBacked.sortedTasks.iterator();
        assertEquals(task1, iterator.next(), "Задача из списка не равна задаче 1");
    }

    @Test
    void shouldReturnPrioritizedTasksWhenPrioritizedTasksContainsSubtask() throws ManagerSaveException {
        assertEquals(0, server.fileBacked.sortedTasks.size(), "Размер списка должен быть равен 0.");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 2);
        Subtask task1 = server.fileBacked.createNewSubtask("Подзадача1", "Описание1", 1,
                epic);
        assertEquals(2, server.fileBacked.sortedTasks.size(), "Размер списка должен быть равен 2.");
        Iterator<Task> iterator =  server.fileBacked.sortedTasks.iterator();
        boolean checkEpic = false;
        boolean checkSubtask = false;
        while (iterator.hasNext()) {
            Task iter = iterator.next();
            if (iter.equals(task1)) {
                checkSubtask = true;
            } else if (iter.equals(epic)) {
                checkEpic = true;
            }
        }
        assertTrue(checkSubtask, "Подзадача из списка не равна подзадаче 1");
        assertTrue(checkEpic, "Эпик из списка не равен эпику 1");
    }

    @Test
    void shouldReturnPrioritizedTasksWhenPrioritizedTasksContainsEpic() throws ManagerSaveException {
        assertEquals(0, server.fileBacked.sortedTasks.size(), "Размер списка должен быть равен 0.");
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 1);
        assertEquals(1, server.fileBacked.sortedTasks.size(), "Размер списка должен быть равен 1.");
        Iterator<Task> iterator = server.fileBacked.sortedTasks.iterator();
        while (iterator.hasNext()) {
            assertEquals(epic, iterator.next(), "Эпик из списка истории не равен эпику 1");
        }
    }

    @Test
    void shouldReturnPrioritizedTasks() throws ManagerSaveException {
        assertEquals(0, server.fileBacked.sortedTasks.size(), "Размер списка должен быть равен 0.");
        Task task = server.fileBacked.createNewTask("Задача1", "Описание1", 1);
        Epic epic = server.fileBacked.createNewEpic("Эпик1", "ОписаниеЭпика1", 2);
        Subtask subtask = server.fileBacked.createNewSubtask("Подзадача1", "ОписаниеПодзадачи1",
                3, epic);
        assertEquals(3, server.fileBacked.sortedTasks.size(), "Размер списка должен быть равен 3.");
        Iterator<Task> iterator = server.fileBacked.sortedTasks.iterator();
        boolean checkTask = false;
        boolean checkEpic = false;
        boolean checkSubtask = false;
        while(iterator.hasNext()) {
            Task iter = iterator.next();
            if (iter.equals(task)) {
                checkTask = true;
            } else if (iter.equals(subtask)) {
               checkSubtask = true;
            } else {
                checkEpic = true;
            }
        }
        assertTrue(checkTask, "Задача из списка не равна задаче 1");
        assertTrue(checkSubtask, "Подзадача из списка не равна подзадаче 1");
        assertTrue(checkEpic, "Эпик из списка не равен эпику 1");
    }*/
}