package tasktracker;

public class Subtask extends Task {
    protected Epic epic;

    public Subtask(String name, String description, long id, Epic epic) {
        super(name, description, id);
        this.epic = epic;
    }

    @Override
    public String toString() {
        return  "Подзадача" +"\n" +
                "Название'" + name + '\'' + "," + "\n" +
                "Описание'" + description + '\'' + "," + "\n" +
                "Статус'" + getStatus() + '\'' + "," + "\n" +
                "id '" + id + '\'';
    }
}
