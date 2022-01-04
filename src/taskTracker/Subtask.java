package taskTracker;

public class Subtask extends Task {

    public Subtask(String name, String description, long id) {
        super(name, description, id);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    public void setDescription(String description) {
        super.setDescription(description);
    }

    @Override
    public void setStatus(String status) {
        super.setStatus(status);
    }

    @Override
    public String getStatus() {
        return super.getStatus();
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
