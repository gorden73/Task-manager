package tasktracker;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    Epic epic = new Epic("a", "b", 1);
    ArrayList<Subtask> subtaskList = epic.getSubtaskList();

    @Test
    public void shouldBeNewWhenSubtaskListIsEmpty() {
        StatusOfTasks epicStatus = epic.getStatus();
        assertEquals(StatusOfTasks.NEW, epicStatus, "Статус эпика должен быть NEW");
    }

    @Test
    public void shouldBeNewWhenAllSubtasksNew() {
        subtaskList.add(new Subtask("a", "a", 2, epic));
        subtaskList.add(new Subtask("b", "b", 3, epic));
        epic.setSubtaskList(subtaskList);
        StatusOfTasks epicStatus = epic.getStatus();
        assertEquals(StatusOfTasks.NEW, epicStatus, "Статус эпика должен быть NEW");
    }

    @Test
    public void shouldBeDoneWhenAllTasksDone() {
        Subtask subtask1 = new Subtask("a", "a", 2, epic);
        Subtask subtask2 = new Subtask("b", "b", 3, epic);
        subtask1.setStatus("DONE");
        subtask2.setStatus("DONE");
        ArrayList<Subtask> newList = new ArrayList<>(List.of(subtask1, subtask2));
        epic.setSubtaskList(newList);
        StatusOfTasks epicStatus = epic.getStatus();
        assertEquals(StatusOfTasks.DONE, epicStatus, "Статус эпика должен быть DONE");
    }

    @Test
    public void shouldBeInProgressWhenTasksNewAndDone() {
        Subtask subtask1 = new Subtask("a", "a", 2, epic);
        Subtask subtask2 = new Subtask("b", "b", 3, epic);
        subtask2.setStatus("DONE");
        ArrayList<Subtask> newList = new ArrayList<>(List.of(subtask1, subtask2));
        epic.setSubtaskList(newList);
        StatusOfTasks epicStatus = epic.getStatus();
        assertEquals(StatusOfTasks.IN_PROGRESS, epicStatus, "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    public void shouldBeInProgressWhenAllTasksInProgress() {
        Subtask subtask1 = new Subtask("a", "a", 2, epic);
        Subtask subtask2 = new Subtask("b", "b", 3, epic);
        subtask1.setStatus("IN_PROGRESS");
        subtask2.setStatus("IN_PROGRESS");
        ArrayList<Subtask> newList = new ArrayList<>(List.of(subtask1, subtask2));
        epic.setSubtaskList(newList);
        StatusOfTasks epicStatus = epic.getStatus();
        assertEquals(StatusOfTasks.IN_PROGRESS, epicStatus, "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    public void shouldReturnSubtaskListWhenIsEmpty() {
        assertEquals(0, subtaskList.size(), "Список подзадач не пустой.");
    }

    @Test
    public void shouldReturnSubtaskListWhenContainsOneElement() {
        Subtask subtask1 = new Subtask("a", "a", 2, epic);
        subtaskList.add(subtask1);
        assertEquals(1, subtaskList.size(), "В списке подзадач не одна подзадача.");
    }

    @Test
    public void shouldReturnSubtaskListWhenContainsMoreThanOneElement() {
        Subtask subtask1 = new Subtask("a", "a", 2, epic);
        Subtask subtask2 = new Subtask("b", "b", 3, epic);
        subtaskList.add(subtask1);
        subtaskList.add(subtask2);
        assertEquals(2, subtaskList.size(), "Список подзадач пустой или " +
                                                            "в списке одна подзадача.");
    }

    @Test
    public void shouldSetSubtaskList() {
        int subtaskListSize = subtaskList.size();
        ArrayList<Subtask> list = new ArrayList<>();
        Subtask subtask1 = new Subtask("a", "a", 2, epic);
        Subtask subtask2 = new Subtask("b", "b", 3, epic);
        list.add(subtask1);
        list.add(subtask2);
        epic.setSubtaskList(list);
        int listSize = epic.getSubtaskList().size();
        assertEquals(0, subtaskListSize, "Список подзадач не пустой.");
        assertEquals(2, list.size(), "В списке подзадач не две задачи.");
        assertEquals(2, listSize, "В списке подзадач не две задачи.");
    }

    @Test
    public void shouldGetStartTimeWhenSubtaskListIsEmpty() {
        LocalDate date = epic.getStartTime();
        assertEquals(Task.DEFAULT_DATE, date, "Дата начала выполнения эпика должна быть 9999-01-01");
    }

    @Test
    public void shouldGetStartTimeWhenSubtaskListContainsOneElement() {
        Subtask subtask1 = new Subtask("a", "a", 2, "15.12.2000", 2, epic);
        subtaskList.add(subtask1);
        LocalDate date = epic.getStartTime();
        assertEquals(subtask1.getStartTime(), date, "Дата начала выполнения эпика должна быть "
                    + subtask1.getStartTime());
    }

    @Test
    public void shouldGetStartTimeWhenSubtaskListContainsMoreThanOneElement() {
        Subtask subtask1 = new Subtask("a", "a", 2, "15.12.2000", 2, epic);
        Subtask subtask2 = new Subtask("c", "d", 3, "22.06.2003", 5, epic);
        subtaskList.add(subtask1);
        subtaskList.add(subtask2);
        LocalDate date = epic.getStartTime();
        assertEquals(subtask1.getStartTime(), date, "Дата начала выполнения эпика должна быть "
                + subtask1.getStartTime());
    }

    @Test
    public void shouldNotSetStartTime() {
        LocalDate date = epic.getStartTime();
        epic.setStartTime("15.12.13");
        assertEquals(Task.DEFAULT_DATE, date, "Дата начала выполнения эпика должна быть 9999-01-01");
    }

    @Test
    public void shouldGetDurationWhenSubtaskListIsEmpty() {
        long duration = epic.getDuration().toDays();
        assertEquals(0, duration, "Продолжительность выполнения эпика должна быть 0 дней");
    }

    @Test
    public void shouldGetDurationWhenSubtaskListContainsOneElement() {
        Subtask subtask1 = new Subtask("a", "a", 2, "15.12.2000", 2, epic);
        subtaskList.add(subtask1);
        long duration = epic.getDuration().toDays();
        assertEquals(subtask1.getDuration().toDays(), duration, "Продолжительность выполнения эпика должна быть"
                    + " " + subtask1.getDuration().toDays() + " дней");
    }

    @Test
    public void shouldGetDurationWhenSubtaskListContainsMoreThanOneElement() {
        Subtask subtask1 = new Subtask("a", "a", 2, "15.12.2000", 2, epic);
        Subtask subtask2 = new Subtask("c", "d", 3, "22.06.2003", 5, epic);
        subtaskList.add(subtask1);
        subtaskList.add(subtask2);
        long duration = epic.getDuration().toDays();
        Duration sum = subtask1.getDuration().plus(subtask2.getDuration());
        assertEquals(sum.toDays(), duration, "Продолжительность выполнения эпика должна быть"
                + " " + sum.toDays() + " дней");
    }

    @Test
    public void shouldNotSetDuration() {
        Duration dur = epic.getDuration();
        epic.setDuration(7);
        assertEquals(0, dur.toDays(), "Продолжительность выполнения эпика должна быть 0 дней");
    }

    @Test
    public void shouldGetEndTimeWhenSubtaskListIsEmpty() {
        LocalDate endTime = epic.getEndTime();
        assertEquals(Task.DEFAULT_DATE, endTime, "Дата окончания выполнения эпика должна быть 9999-01-01");
    }

    @Test
    public void shouldGetEndTimeWhenSubtaskListContainsOneElement() {
        Subtask subtask1 = new Subtask("a", "a", 2, "15.12.2000", 2, epic);
        subtaskList.add(subtask1);
        LocalDate endSub = subtask1.getEndTime();
        LocalDate endTime = epic.getEndTime();
        assertEquals(endSub, endTime, "Дата окончания выполнения эпика должна быть " + endSub);
    }

    @Test
    public void shouldGetEndTimeWhenSubtaskListContainsMoreThanOneElement() {
        Subtask subtask1 = new Subtask("a", "a", 2, "15.12.2000", 2, epic);
        Subtask subtask2 = new Subtask("c", "d", 3, "22.06.2003", 5, epic);
        subtaskList.add(subtask1);
        subtaskList.add(subtask2);
        LocalDate endSub = subtask2.getEndTime();
        LocalDate endTime = epic.getEndTime();
        assertEquals(endSub, endTime, "Дата окончания выполнения эпика должна быть " + endSub);
    }
}