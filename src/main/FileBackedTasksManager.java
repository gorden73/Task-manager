package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;
import tasktracker.TypeOfTasks;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FileBackedTasksManager extends InMemoryTasksManager {
    private static File fileToSave;

    public FileBackedTasksManager(File fileToSave) {
        this.fileToSave = fileToSave;
    }

    public static void main(String[] args) throws IOException, ManagerSaveException {
        FileBackedTasksManager fileBacked = Managers.getBackup(new File("backup.csv"));
        Epic epic = fileBacked.createNewEpic("Second", "epic", 5);
        fileBacked.createNewSubtask("First", "subtask", 6, epic);
        fileBacked.createNewTask("First", "task", 1);
        fileBacked.createNewTask("Second", "task", 2);
        fileBacked.createNewEpic("First", "epic", 4);
        fileBacked.createNewSubtask("Second", "subtask", 7, epic);
        fileBacked.createNewSubtask("Third", "subtask", 8, epic);
        fileBacked.createNewTask("Third", "task", 3);
        fileBacked.getTask(1);
        fileBacked.getTask(3);
        fileBacked.getEpic(5);
        fileBacked.getSubtask(6);
        fileBacked.getEpic(4);
        fileBacked.getEpic(5);
        fileBacked.getSubtask(7);
        fileBacked.getSubtask(8);
        fileBacked.getSubtask(7);
        for (Task t : fileBacked.getHistory()) {
            System.out.println(t);
        }
    }

    private void save() throws ManagerSaveException {
        try (FileWriter writer = new FileWriter(fileToSave, StandardCharsets.UTF_8)) {
            writer.append("id,type,name,status,description,epic" + "\n");
            for (Task task : getTasks().values()) {
                writer.write(toString(task) + "\n");
            }
            for (Task task : getEpics().values()) {
                writer.write(toString(task) + "\n");
            }
            for (Task task : getSubtasks().values()) {
                writer.write(toString(task) + "\n");
            }
            writer.append(" " + "\n");
            for (Task history : getHistory()) {
                Long id1 = history.getId();
                writer.append(id1 + ",");
            }
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    public static FileBackedTasksManager loadFromFile(File file) throws IOException {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);
        List<String> list = new LinkedList<>();
        FileReader reader = new FileReader(file, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(reader);
        while (bufferedReader.ready()) {
            String line = bufferedReader.readLine();
            list.add(line);
        }
        for (int i = 1; i < list.size()-2; i++) {
            Task taskFromFile = taskFromString(list.get(i), fileBackedTasksManager.getEpics());
            if (taskFromFile.getClass().equals(Task.class)) {
                fileBackedTasksManager.setTasks(taskFromFile);
            } else if (taskFromFile.getClass().equals(Subtask.class)) {
                fileBackedTasksManager.setSubtasks((Subtask) taskFromFile);
            } else if (taskFromFile.getClass().equals(Epic.class)) {
                fileBackedTasksManager.setEpics((Epic) taskFromFile);
            }
        }
        List<Long> historyId = fromString(list.get(list.size() - 1));
        for (Long id : historyId) {
            if (fileBackedTasksManager.getTasks().containsKey(id)) {
                fileBackedTasksManager.getHistoryManager().add(fileBackedTasksManager.getTasks().get(id));
            } else if (fileBackedTasksManager.getSubtasks().containsKey(id)) {
                fileBackedTasksManager.getHistoryManager().add(fileBackedTasksManager.getSubtasks().get(id));
            } else if (fileBackedTasksManager.getEpics().containsKey(id)) {
                fileBackedTasksManager.getHistoryManager().add(fileBackedTasksManager.getEpics().get(id));
            }
        }
        bufferedReader.close();
            return fileBackedTasksManager;
    }

    private String toString(Task task) {
        TypeOfTasks type;
        if (getTasks().containsKey(task.getId())) {
            type = TypeOfTasks.TASK;
            return String.format("%d,%S,%s,%S,%s,", task.getId(), type, task.getName(),
                    task.getStatus(), task.getDescription());
        } else if (getSubtasks().containsKey(task.getId())) {
            type = TypeOfTasks.SUBTASK;
            Epic epic = getSubtasks().get(task.getId()).getEpic();
            Long epicId = epic.getId();
            setSubtaskVsEpic(task.getId(), epicId);
            ArrayList<Subtask> subtaskList = epic.getSubtaskList();
            subtaskList.add((Subtask) task);
            epic.setSubtaskList(subtaskList);
            setEpicVsSubtask(epic.getId(), subtaskList);
            return String.format("%d,%S,%s,%S,%s,%s", task.getId(), type, task.getName(),
                    task.getStatus(), task.getDescription(), epicId);
        } else if (getEpics().containsKey(task.getId())){
            type = TypeOfTasks.EPIC;
            return String.format("%d,%S,%s,%S,%s,", task.getId(), type, task.getName(),
                    task.getStatus(), task.getDescription());
        }
        return null;
    }

    private static Task taskFromString(String value, HashMap<Long, Epic> epicForInside) {
        String[] params = value.split(",");
        long parsedId = Long.parseLong(params[0]);
        switch (params[1]) {
            case "SUBTASK":
                Epic epic = epicForInside.get(Long.parseLong(params[5]));
                return new Subtask(params[2], params[4], parsedId, epic);
            case "TASK":
                return new Task(params[2], params[4], parsedId);
            case "EPIC":
                Epic epic1 = new Epic(params[2], params[4], parsedId);
                epicForInside.put(parsedId, epic1);
                return epic1;
        }
        return null;
    }

    private static List<Long> fromString(String value) {
        List<Long> id = new LinkedList<>();
        String[] arrayOfId = value.split(",");
        for(String idList : arrayOfId) {
            if (!idList.isBlank()) {
                id.add(Long.parseLong(idList));
            }
        }
        return id;
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    @Override
    public Epic createNewEpic(String inputName, String inputDescription, long id) throws ManagerSaveException {
        Epic newEpic = super.createNewEpic(inputName, inputDescription, id);
        save();
        return newEpic;
    }

    @Override
    public Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic)
                                    throws ManagerSaveException {
        Subtask newSubtask = super.createNewSubtask(inputName, inputDescription, id, epic);
        save();
        return newSubtask;
    }

    @Override
    public Task createNewTask(String inputName, String inputDescription, long id) throws ManagerSaveException {
        Task newTask = super.createNewTask(inputName, inputDescription, id);
        save();
        return newTask;
    }

    @Override
    public void updateTask(long inputId, Task task) throws ManagerSaveException {
        super.updateTask(inputId, task);
        save();
    }

    @Override
    public void updateSubtask(long inputId, Subtask subtask) throws ManagerSaveException {
        super.updateSubtask(inputId, subtask);
        save();
    }

    @Override
    public void updateEpic(long inputId, Epic epic) throws ManagerSaveException {
        super.updateEpic(inputId, epic);
        save();
    }

    @Override
    public Task getTask(long inputId) throws ManagerSaveException {
        Task task = super.getTask(inputId);
        save();
        return task;
    }

    @Override
    public Subtask getSubtask(long inputId) throws ManagerSaveException {
        Subtask subtask = super.getSubtask(inputId);
        save();
        return subtask;
    }

    @Override
    public Epic getEpic(long inputId) throws ManagerSaveException {
        Epic epic = super.getEpic(inputId);
        save();
        return epic;
    }

    @Override
    public void removeTask(long inputId) throws ManagerSaveException {
        super.removeTask(inputId);
        save();
    }

    @Override
    public void removeEpic(long inputId) throws ManagerSaveException {
        super.removeEpic(inputId);
        save();
    }

    @Override
    public void removeSubtask(long inputId) throws ManagerSaveException {
        super.removeSubtask(inputId);
        save();
    }

    @Override
    public void removeAllTasks(HashMap<Long, Task> tasks, HashMap<Long, Subtask> subtasks, HashMap<Long, Epic> epics,
                               HashMap<Long, ArrayList<Subtask>> epicVsSubtask, HashMap<Long, Long> subtaskVsEpic)
                               throws ManagerSaveException {
        super.removeAllTasks(tasks, subtasks, epics, epicVsSubtask, subtaskVsEpic);
        save();
    }
}
