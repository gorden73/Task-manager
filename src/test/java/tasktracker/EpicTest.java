package tasktracker;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    Epic epic = new Epic("a", "b", 1);
    ArrayList<Subtask> subtaskList = epic.getSubtaskList();

    @Test
    public void shouldBeNewWhenSubtaskListIsEmpty() {
        StatusOfTasks epicStatus = epic.getStatus();
        assertEquals(StatusOfTasks.NEW, epicStatus);
    }

    @Test
    public void shouldBeNewWhenAllSubtasksNew() {
        subtaskList.add(new Subtask("a", "a", 2, epic));
        subtaskList.add(new Subtask("b", "b", 3, epic));
        epic.setSubtaskList(subtaskList);
        StatusOfTasks epicStatus = epic.getStatus();
        assertEquals(StatusOfTasks.NEW, epicStatus);
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
        assertEquals(StatusOfTasks.DONE, epicStatus);
    }

    @Test
    public void shouldBeInProgressWhenTasksNewAndDone() {
        Subtask subtask1 = new Subtask("a", "a", 2, epic);
        Subtask subtask2 = new Subtask("b", "b", 3, epic);
        subtask2.setStatus("DONE");
        ArrayList<Subtask> newList = new ArrayList<>(List.of(subtask1, subtask2));
        epic.setSubtaskList(newList);
        StatusOfTasks epicStatus = epic.getStatus();
        assertEquals(StatusOfTasks.IN_PROGRESS, epicStatus);
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
        assertEquals(StatusOfTasks.IN_PROGRESS, epicStatus);
    }
}