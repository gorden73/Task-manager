package main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.Epic;
import tasktracker.StatusOfTasks;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/*abstract*/ class TaskManagerTest/*<T extends TaskManager> */ {

        TaskManager taskManager;

    @BeforeEach
    void start() throws IOException {
        taskManager = new InMemoryTasksManager();
    }

    @Test
    public void createNewSubtask() throws ManagerSaveException {
        Subtask subtask = taskManager.createNewSubtask("a", "b", 2,
                new Epic("c", "d", 1));
        assertNotNull(taskManager.getSubtask(2), "Подзадача не найдена.");
        assertNotNull(subtask.getEpic());
        assertEquals(subtask, taskManager.getSubtask(2), "Подзадачи не совпадают.");
        assertTrue(subtask.getEpic().getSubtaskList().contains(subtask),
                "Список внутри эпика не содержит задачу.");
        Subtask subtask1 = taskManager.createNewSubtask("C", "D", 3, "09.08.2002",
                3, new Epic("c", "d", 4));
        assertNotNull(taskManager.getSubtask(3), "Подзадача не найдена.");
        assertEquals(subtask1, taskManager.getSubtask(3), "Подзадачи не совпадают.");
        assertTrue(subtask1.getEpic().getSubtaskList().contains(subtask1),
                "Список внутри эпика не содержит задачу.");
    }

    @Test
    public void createNewTask() throws ManagerSaveException {
        Task task = taskManager.createNewTask("a", "b", 2);
        assertNotNull(taskManager.getTask(2), "Задача не найдена.");
        assertEquals(task, taskManager.getTask(2), "Задачи не совпадают.");
        Task task1 = taskManager.createNewTask("F", "S", 3);
        assertNotNull(taskManager.getTask(3), "Задача не найдена.");
        assertEquals(task1, taskManager.getTask(3), "Задачи не совпадают.");
    }

    @Test
    public void createNewEpic() throws ManagerSaveException {
        Epic epic = taskManager.createNewEpic("a", "b", 2);
        assertNotNull(taskManager.getEpic(2L), "Подзадача не найдена.");
        assertEquals(epic, taskManager.getEpic(2L), "Подзадачи не совпадают.");
    }

    @Test
    public void setStartTime() throws ManagerSaveException {
        Task task = taskManager.createNewTask("a", "b", 1, "09.08.2002", 3);
        Epic epic = taskManager.createNewEpic("a", "b", 2);
        epic.setStartTime("01.02.2003");
        assertEquals(LocalDate.parse("9999-01-01"), epic.getStartTime(),
                "Дата начала выполнения эпика не совпадает с заданной.");
        Subtask subtask = taskManager.createNewSubtask("a", "b", 3, "05.07.2005",
                                               3, epic);
        task.setStartTime("03.02.2001");
        subtask.setStartTime("07.11.2013");
        assertEquals(LocalDate.parse("2001-02-03"), task.getStartTime(),
                "Дата начала выполнения задачи не совпадает с заданной.");
        assertEquals(LocalDate.parse("2013-11-07"), subtask.getStartTime(),
                "Дата начала выполнения подзадачи не совпадает с заданной.");
    }

    @Test
    public void setDuration() throws ManagerSaveException {
        Task task = taskManager.createNewTask("a", "b", 1, "09.08.2002", 3);
        Epic epic = taskManager.createNewEpic("a", "b", 2);
        epic.setDuration(5);
        assertEquals(0, epic.getDuration().toDays(),
                "Продолжительность выполнения эпика не совпадает с заданной.");
        Subtask subtask = taskManager.createNewSubtask("a", "b", 3, "05.07.2005",
                3, epic);
        task.setDuration(1);
        subtask.setDuration(4);
        assertEquals(1, task.getDuration().toDays(),
                "Продолжительность выполнения задачи не совпадает с заданной.");
        assertEquals(4, subtask.getDuration().toDays(),
                "Продолжительность выполнения подзадачи не совпадает с заданной.");
    }

    @Test
    public void setStatus() throws ManagerSaveException {
        Task task = taskManager.createNewTask("a", "b", 1, "09.08.2002", 3);
        Epic epic = taskManager.createNewEpic("a", "b", 2);
        //у эпика setStatus() проверил в классе EpicTest
        Subtask subtask = taskManager.createNewSubtask("a", "b", 3, "05.07.2005",
                3, epic);
        task.setStatus("NEW");
        assertEquals(StatusOfTasks.NEW, task.getStatus(), "Статус задачи должен быть NEW.");
        task.setStatus("IN_PROGRESS");
        assertEquals(StatusOfTasks.IN_PROGRESS, task.getStatus(), "Статус задачи должен быть IN_PROGRESS.");
        task.setStatus("DONE");
        assertEquals(StatusOfTasks.DONE, task.getStatus(), "Статус задачи должен быть DONE.");
        subtask.setStatus("NEW");
        assertEquals(StatusOfTasks.NEW, subtask.getStatus(), "Статус задачи должен быть NEW.");
        subtask.setStatus("IN_PROGRESS");
        assertEquals(StatusOfTasks.IN_PROGRESS, subtask.getStatus(), "Статус задачи должен быть IN_PROGRESS.");
        subtask.setStatus("DONE");
        assertEquals(StatusOfTasks.DONE, subtask.getStatus(), "Статус задачи должен быть DONE.");
    }

    @Test
    public void getTasks() {
        assertEquals(0, taskManager.getTasks().size(), "Таблица задач должна быть пустой.");
        Task task = new Task("A", "B", 1);
        taskManager.setTasks(task);
        assertEquals(1, taskManager.getTasks().size(), "Таблица задач должна содержать 1 задачу.");
        assertEquals(task, taskManager.getTasks().get(1L), "Задачи не совпадают.");
        Task task1 = new Task("С", "В", 2);
        taskManager.setTasks(task1);
        assertEquals(2, taskManager.getTasks().size(), "Таблица задач должна содержать 1 задачу.");
        assertEquals(task1, taskManager.getTasks().get(2L), "Задачи не совпадают.");
    }

    @Test
    public void getSubtasks() {
        assertEquals(0, taskManager.getSubtasks().size(), "Таблица подзадач должна быть пустой.");
        Subtask subtask = new Subtask("A", "B", 1, new Epic("D", "F", 2));
        taskManager.setSubtasks(subtask);
        assertEquals(1, taskManager.getSubtasks().size(), "Таблица подзадач должна содержать 1 задачу.");
        assertEquals(subtask, taskManager.getSubtasks().get(1L), "Подзадачи не совпадают.");
        Subtask subtask1 = new Subtask("С", "В", 3, new Epic("D", "F", 4));
        taskManager.setSubtasks(subtask1);
        assertEquals(2, taskManager.getSubtasks().size(), "Таблица подзадач должна содержать 2 задачи.");
        assertEquals(subtask1, taskManager.getSubtasks().get(3L), "Подзадачи не совпадают.");
    }

    @Test
    public void getEpics() {
        assertEquals(0, taskManager.getEpics().size(), "Таблица эпиков должна быть пустой.");
        Epic epic = new Epic("A", "B", 1);
        taskManager.setEpics(epic);
        assertEquals(1, taskManager.getEpics().size(), "Таблица эпиков должна содержать 1 эпик.");
        assertEquals(epic, taskManager.getEpics().get(1L), "Эпики не совпадают.");
        Epic epic1 = new Epic("С", "В", 3);
        taskManager.setEpics(epic1);
        assertEquals(2, taskManager.getEpics().size(), "Таблица эпиков должна содержать 2 эпика.");
        assertEquals(epic1, taskManager.getEpics().get(3L), "Эпики не совпадают.");
    }
    //setTasks(), setSubtasks(), setEpics() проверяются в аналогичных геттерах выше

    @Test
    public void getHistoryManager() {
        HistoryManager historyManager = taskManager.getHistoryManager();
        assertEquals(InMemoryHistoryManager.class, historyManager.getClass());
    }

    /*@Test
    public void setTasks() {
    }
    @Test
    public void setSubtasks() {
    }
    @Test
    public void setEpics() {
    }*/

    @Test
    public void getHistory() throws ManagerSaveException {
        assertEquals(0, taskManager.getHistory().size(), "Список должен быть пустым.");
        Task task = taskManager.createNewTask("a", "b", 1, "09.08.2002", 3);
        Epic epic = taskManager.createNewEpic("a", "b", 2);
        Subtask subtask = taskManager.createNewSubtask("a", "b", 3, "05.07.2005",
                3, epic);
        taskManager.getTask(1);
        assertEquals(1, taskManager.getHistory().size(), "Список должен содержать 1 задачу.");
        assertEquals(task, taskManager.getHistory().get(0), "Задачи не совпадают.");
        taskManager.getEpic(2);
        assertEquals(2, taskManager.getHistory().size(), "Список должен содержать 2 задачи.");
        assertEquals(task, taskManager.getHistory().get(0), "Задачи не совпадают.");
        assertEquals(epic, taskManager.getHistory().get(1), "Задачи не совпадают.");
        taskManager.getSubtask(3);
        assertEquals(3, taskManager.getHistory().size(), "Список должен содержать 3 задачи.");
        assertEquals(task, taskManager.getHistory().get(0), "Задачи не совпадают.");
        assertEquals(epic, taskManager.getHistory().get(1), "Задачи не совпадают.");
        assertEquals(subtask, taskManager.getHistory().get(2), "Задачи не совпадают.");
    }

    @Test
    public void getTask() throws ManagerSaveException {
        Task task = taskManager.createNewTask("a", "b", 1, "09.08.2002", 3);
        assertEquals(task, taskManager.getTask(1), "Задачи не совпадают.");
    }

    @Test
    public void getSubtask() throws ManagerSaveException {
        Epic epic = taskManager.createNewEpic("a", "b", 2);
        Subtask subtask = taskManager.createNewSubtask("a", "b", 3, "05.07.2005",
                3, epic);
        assertEquals(subtask, taskManager.getSubtask(3), "Задачи не совпадают.");
    }

    @Test
    public void getEpic() throws ManagerSaveException {
        Epic epic = taskManager.createNewEpic("a", "b", 2);
        assertEquals(epic, taskManager.getEpic(2), "Задачи не совпадают.");
    }

    @Test
    public void updateTask() throws ManagerSaveException {
        Task task = taskManager.createNewTask("a", "b", 1, "09.08.2002", 3);
        Task task1 = taskManager.createNewTask("r", "t", 2, "05.03.2022", 5);
        taskManager.updateTask(1, task1);
        assertEquals(task1.getName(), task.getName(), "Названия не совпадают.");
        assertEquals(task1.getDescription(), task.getDescription(), "Описание не совпадает.");
        assertEquals(task1.getStatus(), task.getStatus(), "Статусы не совпадают.");
        assertEquals(task1.getStartTime(), task.getStartTime(), "Даты начала выполнения задачи не совпадают.");
        assertEquals(task1.getDuration(), task.getDuration(), "Продолжительности не совпадают.");
        assertNotEquals(task1.getId(), task.getId(), "Id не должны совпадать.");
    }

    @Test
    public void updateSubtask() throws ManagerSaveException {
        Epic epic = taskManager.createNewEpic("a", "b", 1);
        Subtask subtask = taskManager.createNewSubtask("a", "b", 2, "05.07.2005",
                3, epic);
        Subtask subtask1 = taskManager.createNewSubtask("t", "y", 3, "11.11.2015",
                6, epic);
        taskManager.updateSubtask(2, subtask1);
        assertEquals(epic, subtask.getEpic());
        assertEquals(subtask1.getName(), subtask.getName(), "Названия не совпадают.");
        assertEquals(subtask1.getDescription(), subtask.getDescription(), "Описание не совпадает.");
        assertEquals(subtask1.getStatus(), subtask.getStatus(), "Статусы не совпадают.");
        assertEquals(subtask1.getStartTime(), subtask.getStartTime(),
                "Даты начала выполнения задачи не совпадают.");
        assertEquals(subtask1.getDuration(), subtask.getDuration(), "Продолжительности не совпадают.");
        assertNotEquals(subtask1.getId(), subtask.getId(), "Id не должны совпадать.");
    }

    @Test
    public void updateEpic() throws ManagerSaveException {
        Epic epic = taskManager.createNewEpic("a", "b", 1);
        Epic epic1 = taskManager.createNewEpic("e", "q", 2);
        taskManager.updateEpic(1, epic1);
        assertEquals(epic1.getName(), epic.getName(), "Названия не совпадают.");
        assertEquals(epic1.getDescription(), epic.getDescription(), "Описание не совпадает.");
        assertNotEquals(epic1.getId(), epic.getId(), "Id не должны совпадать.");
    }

    @Test
    public void removeTask() throws ManagerSaveException {
        Task task = taskManager.createNewTask("a", "b", 1, "09.08.2002", 3);
        assertEquals(task, taskManager.getTask(1));
        taskManager.removeTask(1);
        assertNull(taskManager.getTask(1), "Задача не удалена.");
    }

    @Test
    public void removeEpic() throws ManagerSaveException {
        Epic epic = taskManager.createNewEpic("a", "b", 1);
        assertEquals(epic, taskManager.getEpic(1));
        taskManager.removeEpic(1);
        assertNull(taskManager.getEpic(1), "Задача не удалена.");
    }

    @Test
    public void removeSubtask() throws ManagerSaveException {
        Epic epic = taskManager.createNewEpic("a", "b", 1);
        Subtask subtask = taskManager.createNewSubtask("a", "b", 2, "05.07.2005",
                3, epic);
        assertEquals(subtask, taskManager.getSubtask(2));
        assertTrue(epic.getSubtaskList().contains(subtask));
        taskManager.removeSubtask(2);
        assertNull(taskManager.getSubtask(2), "Задача не удалена.");
        assertFalse(epic.getSubtaskList().contains(subtask), "Задача не удалена из списка внутри эпика.");
    }

    @Test
    public void removeAllTasks() throws ManagerSaveException {
        assertEquals(0, taskManager.getTasks().size());
        assertEquals(0, taskManager.getSubtasks().size());
        assertEquals(0, taskManager.getEpics().size());
        //assertEquals(0, taskManager.getEpicVsSubtask().size());
        //assertEquals(0, taskManager.getSubtaskVsEpic().size());
        Epic epic = taskManager.createNewEpic("a", "b", 1);
        Subtask subtask = taskManager.createNewSubtask("a", "b", 2, "05.07.2005",
                3, epic);
        Task task = taskManager.createNewTask("a", "b", 3, "09.08.2002", 3);
        assertEquals(task, taskManager.getTask(3));
        assertEquals(subtask, taskManager.getSubtask(2));
        assertEquals(epic, taskManager.getEpic(1));
        assertEquals(1, taskManager.getTasks().size());
        assertEquals(1, taskManager.getSubtasks().size());
        assertEquals(1, taskManager.getEpics().size());
        //assertEquals(1, taskManager.getEpicVsSubtask().size());
        //assertEquals(1, taskManager.getSubtaskVsEpic().size());
        taskManager.removeAllTasks(taskManager.getTasks(), taskManager.getSubtasks(), taskManager.getEpics()
                /*taskManager.getEpicVsSubtask(), taskManager.getSubtaskVsEpic()*/);
        assertEquals(0, taskManager.getTasks().size());
        assertEquals(0, taskManager.getSubtasks().size());
        assertEquals(0, taskManager.getEpics().size());
        //assertEquals(0, taskManager.getEpicVsSubtask().size());
        //assertEquals(0, taskManager.getSubtaskVsEpic().size());
    }
}