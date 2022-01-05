package taskTracker;

import java.util.ArrayList;

public class Epic extends Task {
    protected ArrayList<Long> subtaskList = new ArrayList<>();

    public ArrayList<Long> getSubtaskList() {
        return subtaskList;
    }

    public void setSubtaskList(ArrayList<Long> subtaskList) {
        this.subtaskList = subtaskList;
    }

    public Epic(String name, String description, long id) {
        super(name, description, id);
    }

    public String getStatus(Manager manager, long inputId, String answer) {
        Long epicId = manager.getSubtaskVsEpic().get(inputId);
        Epic epic = manager.getEpics().get(epicId);
        if (answer.equals("NEW") || answer.equals("IN_PROGRESS") || answer.equals("DONE")) {
            epic.setStatus("IN_PROGRESS");
            if (answer.equals("NEW") || answer.equals("DONE")) {
                int count = 0;
                for (Subtask sub : manager.getSubtasks().values()) {
                    if (sub.getStatus().equals(answer)) {
                        ArrayList<Long> idSub = manager.getEpicVsSubtask().get(epicId);
                        for (Long newId : idSub) {
                            if (sub.getId().equals(newId)) {
                                count++;
                            }
                        }
                    }
                }
                if (count == manager.getEpicVsSubtask().size()) {
                    epic.setStatus(answer);
                }
            }
        }
        return super.getStatus();
    }

    @Override
    public void setStatus(String status) {
        super.setStatus(status);
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
