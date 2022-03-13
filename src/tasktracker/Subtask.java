package tasktracker;

public class Subtask extends Task {
    protected Epic epic;

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
}
