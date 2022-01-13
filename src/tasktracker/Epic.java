package tasktracker;

import java.util.ArrayList;

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
                "id '" + id + '\'';
    }
}
