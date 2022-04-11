package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

class HttpTaskServerTest extends TaskManagerTest<TaskManager> {
    Gson gson;

    @BeforeEach
    void started() {
        gson = server.fileBacked.getGson();
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
    void shouldNotReturnTaskByIdWhenGetRequestAndTaskIsMissing() throws ManagerSaveException, IOException,
                                                                        InterruptedException {
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
}