package taskTracker;

public class Subtask extends Task {

    public Subtask(String name, String description, long id) {
        super(name, description, id);
    }

    @Override
    public String toString() {
        return  "Подзадача" +"\n" +
                "Название'" + name + '\'' + "," + "\n" +
                "Описание'" + description + '\'' + "," + "\n" +
                "Статус'" + status + '\'' + "," + "\n" +
                "id '" + id + '\'';
    }
}
