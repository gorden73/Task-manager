package tasktracker;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedList;

public class Epic extends Task {
    protected LinkedList<Subtask> subtaskList = new LinkedList<>();

    public LinkedList<Subtask> getSubtaskList() {
        return subtaskList;
    }

    public void setSubtaskList(LinkedList<Subtask> subtaskList) {
        this.subtaskList = subtaskList;
    }

    public Epic(String name, String description, long id) {
        super(name, description, id, "01.01.01", 0);
    }

    @Override
    public LocalDate getStartTime() {
        if (subtaskList.isEmpty()) {
            return LocalDate.of(01, 01, 01);
        }
        return subtaskList.getFirst().getStartTime();
    }

    @Override
    public void setStartTime(String startTime) {
    }

    @Override
    public Duration getDuration() {
        Duration generalDuration = Duration.ofDays(0);
        for (Subtask sub : subtaskList) {
            generalDuration = generalDuration.plus(sub.getDuration());
        }
        return generalDuration;
    }

    @Override
    public void setDuration(int duration) {
    }

    @Override
    public StatusOfTasks getStatus() {
        int count = 0;
        int count1 = 0;

        for (Subtask sub : subtaskList) {
            if (sub.getStatus().equals(StatusOfTasks.NEW)) {
                count++;
            } else if (sub.getStatus().equals(StatusOfTasks.DONE)) {
                count1++;
            }
        }
        if (count == subtaskList.size()) {
            return StatusOfTasks.NEW;
        } else if (count1 == subtaskList.size()) {
            return StatusOfTasks.DONE;
        } else {
            return StatusOfTasks.IN_PROGRESS;
        }
    }

    @Override
    public void setStatus(String status) {
    }

    @Override
    public String toString() {
        return  "Эпик" + "\n" +
                "Название'" + name + '\'' + "," + "\n" +
                "Описание'" + description + '\'' + "," + "\n" +
                "Статус'" + getStatus() + '\'' + "," + "\n" +
                "Дата начала выполнения задачи'" + getStartTime() + '\'' + "," + "\n" +
                "Количество дней на выполнение задачи'" + getDuration().toDays() + '\'' + "," + "\n" +
                "id '" + id + '\'';
    }
}
