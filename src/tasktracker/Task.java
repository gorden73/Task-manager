package tasktracker;

public class Task {
    protected String name;
    protected String description;
    private StatusOfTasks status;
    protected final long id;

    public Task(String name, String description, long id) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.status = StatusOfTasks.NEW;
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
