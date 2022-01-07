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
    public void setStatus(String status) {
        if (status.equals("NEW")) {
            int count = 0;
            for (Subtask sub : subtaskList) {
                if (sub.getStatus().equals(status)) {
                    count++;
                }
            }
            if (count == subtaskList.size()) {
                super.setStatus(status);
            } else {
                super.setStatus("IN_PROGRESS");
            }
        } else if (status.equals("DONE")) {
            int count = 0;
            for (Subtask sub : subtaskList) {
                if (sub.getStatus().equals(status)) {
                    count++;
                }
            }
            if (count == subtaskList.size()) {
                super.setStatus(status);
            } else {
                super.setStatus("IN_PROGRESS");
            }
        } else if (status.equals("IN_PROGRESS")) {
            super.setStatus("IN_PROGRESS");
        }
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
