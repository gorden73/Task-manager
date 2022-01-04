package taskTracker;

public class Epic extends Task {

    public Epic(String name, String description, long id) {
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
    public Long getId() {
        return super.getId();
    }

    @Override
    public String toString() {
        return  "Эпик" + "\n" +
                "Название'" + name + '\'' + "," + "\n" +
                "Описание'" + description + '\'' + "," + "\n" +
                "Статус'" + status + '\'' + "," + "\n" +
                "id '" + id + '\'';
    }
}
