package tasktracker;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Task {
    protected String name;
    protected String description;
    private StatusOfTasks status;
    protected final long id;
    private LocalDate startTime;
    private Duration duration;
    public static final LocalDate DEFAULT_DATE = LocalDate.of(9999, 01, 01);
    public static final Duration DEFAULT_DURATION = Duration.ofDays(0);

    public Task(String name, String description, long id) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = StatusOfTasks.NEW;
    }

    public Task(String name, String description, long id, String startTime, int duration) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = StatusOfTasks.NEW;
        this.startTime = LocalDate.parse(startTime, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        this.duration = Duration.ofDays(duration);
    }

    @Override
    public String toString() {
        return  "Задача" +"\n" +
                "Название'" + name + '\'' + "," + "\n" +
                "Описание'" + description + '\'' + "," + "\n" +
                "Статус'" + getStatus() + '\'' + "," + "\n" +
                "Дата начала выполнения задачи'" + getStartTime() + '\'' + "," + "\n" +
                "Количество дней на выполнение задачи'" + getDuration().toDays() + '\'' + "," + "\n" +
                "id '" + id + '\'';
    }

    public LocalDate getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = LocalDate.parse(startTime, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = Duration.ofDays(duration);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public StatusOfTasks getStatus() {
        return status;
    }

    public void setStatus(String status) {
        switch (status) {
            case "NEW":
                this.status = StatusOfTasks.NEW;
                break;
            case "IN_PROGRESS":
                this.status = StatusOfTasks.IN_PROGRESS;
                break;
            case "DONE":
                this.status = StatusOfTasks.DONE;
                break;
            default:
                System.out.println("Такого статуса нет");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
