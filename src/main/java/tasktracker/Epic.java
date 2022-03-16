package tasktracker;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    protected ArrayList<Subtask> subtaskList = new ArrayList<>();

    public ArrayList<Subtask> getSubtaskList() {
        return subtaskList;
    }

    public void setSubtaskList(ArrayList<Subtask> subtaskList) {
        this.subtaskList = subtaskList;
    }

    public Epic(String name, String description, long id) {
        super(name, description, id);
    }

    @Override
    public LocalDate getStartTime() {
        if (subtaskList.isEmpty()) {
            return null;
        } else if (subtaskList.size() == 1) {
            return subtaskList.get(0).getStartTime();
        }
        subtaskList.sort((s1, s2) -> {
            if (s1.getStartTime().isAfter(s2.getStartTime())) {
                return 1;
            } else if (s1.getStartTime().isBefore(s2.getStartTime())) {
                return -1;
            } else {
                return 0;
            }
        });
        int i = 0;
        while (subtaskList.get(i).getStartTime().equals(Task.DEFAULT_DATE)) {
            i++;
        }
        return subtaskList.get(i).getStartTime();
    }

    @Override
    public void setStartTime(String startTime) {
    }

    @Override
    public Duration getDuration() {
        if (subtaskList.isEmpty()) {
            return null;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        if (Objects.equals(this.getId(), epic.getId())) return true;
        return Objects.equals(subtaskList, epic.subtaskList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subtaskList);
    }
}
