package tasktracker;

public class Task {
    protected String name;
    protected String description;
    private String status;
    protected long id;

    public Task(String name, String description, long id) {
        this.name = name;
        this.description = description;
        this.id = id;
        status = "NEW";
    }

    @Override
    public String toString() {
        return  "Задача" +"\n" +
                "Название'" + name + '\'' + "," + "\n" +
                "Описание'" + description + '\'' + "," + "\n" +
                "Статус'" + getStatus() + '\'' + "," + "\n" +
                "id '" + id + '\'';
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
