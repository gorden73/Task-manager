package tasktracker;

import java.util.Objects;

public class Subtask extends Task {
    protected Epic epic;

    public Subtask(String name, String description, long id, Epic epic) {
        super(name, description, id);
        this.epic = epic;
    }

    public Subtask(String name, String description, long id, String startTime, int duration, Epic epic) {
        super(name, description, id, startTime, duration);
        this.epic = epic;
    }

    @Override
    public String toString() {
        return  "Подзадача" +"\n" +
                "Название'" + name + '\'' + "," + "\n" +
                "Описание'" + description + '\'' + "," + "\n" +
                "Статус'" + getStatus() + '\'' + "," + "\n" +
                "Дата начала выполнения задачи'" + getStartTime() + '\'' + "," + "\n" +
                "Количество дней на выполнение задачи'" + getDuration().toDays() + '\'' + "," + "\n" +
                "id '" + id + '\'';
    }

    public Epic getEpic() {
        return this.epic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        if (Objects.equals(this.getId(), subtask.getId())) return true;
        return Objects.equals(epic, subtask.epic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epic);
    }
}
