package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;
import tasktracker.TypeOfTasks;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FileBackedTasksManager extends InMemoryTasksManager {
    private File fileToSave;

    public FileBackedTasksManager(File fileToSave) {
        this.fileToSave = fileToSave;
    }

    public static void main(String[] args) throws IOException, ManagerSaveException {
        FileBackedTasksManager fileBacked = Managers.getBackup(new File("backup.csv"));
        fileBacked.createNewTask("First", "task", 1, "17.01.2012", 3);
        fileBacked.createNewTask("Second", "task", 2, "08.03.2022", 2);
        fileBacked.createNewTask("Third", "task", 10);
        fileBacked.createNewEpic("First", "epic", 4);
        fileBacked.createNewTask("Third", "task", 3, "31.07.2015", 9);
        Epic epic = fileBacked.createNewEpic("Second", "epic", 5);
        fileBacked.createNewSubtask("First", "subtask", 6, epic, "25.04.2013", 2);
        fileBacked.createNewSubtask("Second", "subtask", 7, epic, "13.06.2015", 4);
        fileBacked.createNewSubtask("Third", "subtask", 8, epic,"29.12.2011", 6);
        fileBacked.createNewSubtask("Fourth", "subtask", 9, epic);
        /*fileBacked.getTask(1);
        fileBacked.getTask(3);
        fileBacked.getEpic(5);
        fileBacked.getSubtask(6);
        fileBacked.getEpic(4);
        fileBacked.getEpic(5);
        fileBacked.getSubtask(7);
        fileBacked.getSubtask(8);
        fileBacked.getSubtask(7);*/
        /*for (Task t : fileBacked.getHistory()) {
            System.out.println(t);
        }*/
        for (Task task1 : fileBacked.getPrioritizedTasks()) {
            System.out.println(task1.getStartTime() + " " + task1.getId());
        }
        System.out.println("Размер списка");
        System.out.println(fileBacked.getPrioritizedTasks().size());
        System.out.println(fileBacked.getEpics().size());
        //fileBacked.createNewSubtask("a", "b", 11, "12.12.2012", 6, fileBacked.getEpics().get(4L));
        fileBacked.createNewSubtask("a", "b", 17, fileBacked.getEpics().get(4L), "12.12.2012", 1);
        fileBacked.createNewSubtask("a", "b", 18, fileBacked.getEpics().get(4L), "12.12.2012", 2);
        //System.out.println(fileBacked.getTask(3).getDuration().toDays());
        //System.out.println(fileBacked.getEpic(5).getDuration().toDays());
        //fileBacked.updateSubtask(7, new Subtask("Updated", "Subtask",
         //       7, "20.06.16", 6, epic));
        //fileBacked.removeSubtask(8);
    }

    private TreeSet<Task> getPrioritizedTasks() {
        return sortedTasks;
    }

    private void save() throws ManagerSaveException {
        try (FileWriter writer = new FileWriter(fileToSave, StandardCharsets.UTF_8)) {
            writer.append("id,type,name,status,description,epic,startTime,duration," + "\n");
            for (Task task : getTasks().values()) {
                writer.write(toString(task) + "\n");
            }
            for (Task task : getEpics().values()) {
                writer.write(toString(task) + "\n");
                //
                /*sortedTasks.remove(task);
                sortedTasks.add(task);*/
                //ДОБАВИЛ КОД ДЛЯ СОРТИРОВКИ ЭПИКОВ
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
        try {
            FileReader reader = new FileReader(file, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(reader);
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine();
                list.add(line);
            }
            for (int i = 1; i < list.size()-1; i++) {
                if (list.get(i).isBlank()) {
                    break;
                }
                Task taskFromFile = taskFromString(list.get(i), fileBackedTasksManager.getEpics());
                fileBackedTasksManager.sortedTasks.add(taskFromFile);
                if (taskFromFile.getClass().equals(Task.class)) {
                    fileBackedTasksManager.setTasks(taskFromFile);
                } else if (taskFromFile.getClass().equals(Subtask.class)) {
                    fileBackedTasksManager.setSubtasks((Subtask) taskFromFile);

                } else if (taskFromFile.getClass().equals(Epic.class)) {
                    fileBackedTasksManager.setEpics((Epic) taskFromFile);
                }
            }
            //
            /*for (Epic e : fileBackedTasksManager.getEpics().values()) {
                if (!e.getSubtaskList().isEmpty()) {
                    fileBackedTasksManager.sortedTasks.remove(e);
                    //fileBackedTasksManager.sortedTasks.add(e);
                }
            }*/
            //ДОБАВИЛ ЗДЕСЬ УДАЛЕНИЕ ЭПИКА И ПОВТОРНОЕ ДОБАВЛЕНИЕ В SORTEDTASKS ПРИ ДОБАВЛЕНИИ САБТАСКИ
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
        } catch (NullPointerException e) {
            System.out.println("Отсутствует значение параметра. Проверьте введенные данные.");
            e.printStackTrace();
        }
            return fileBackedTasksManager;
    }

    private String toString(Task task) {
        TypeOfTasks type;
        if (getTasks().containsKey(task.getId())) {
            type = TypeOfTasks.TASK;
            return String.format("%d,%S,%s,%S,%s,%s,%s", task.getId(), type, task.getName(),
                    task.getStatus(), task.getDescription(), task.getStartTime().toString(),
                    task.getDuration().toDays());
        } else if (getSubtasks().containsKey(task.getId())) {
            type = TypeOfTasks.SUBTASK;
            Epic epic = getSubtasks().get(task.getId()).getEpic();
            Long epicId = epic.getId();
            return String.format("%d,%S,%s,%S,%s,%s,%s,%s", task.getId(), type, task.getName(),
                    task.getStatus(), task.getDescription(), epicId, task.getStartTime().toString(),
                    task.getDuration().toDays());
        } else if (getEpics().containsKey(task.getId())){
            type = TypeOfTasks.EPIC;
            //
            String ep = String.format("%d,%S,%s,%S,%s,%s,%s", task.getId(), type, task.getName(),
                    task.getStatus(), task.getDescription(), task.getStartTime().toString(),
                    task.getDuration().toDays());
            /*sortedTasks.remove(task);
            sortedTasks.add(task);*/
            //ДОБАВИЛ КОД ДЛЯ СОРТИРОВКИ ЭПИКОВ
            return ep;
        }
        return null;
    }

    private static Task taskFromString(String value, HashMap<Long, Epic> epicForInside) {
        String[] params = value.split(",");
        try {
            long parsedId = Long.parseLong(params[0]);
            switch (params[1]) {
                case "SUBTASK":
                    Epic epic = epicForInside.get(Long.parseLong(params[5]));
                    LocalDate time = LocalDate.parse(params[6]);
                    String start = time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    ArrayList<Subtask> subList = epic.getSubtaskList();
                    Subtask subFromFile = new Subtask(params[2], params[4], parsedId, epic, start,
                                                      Integer.parseInt(params[7]));
                    subList.add(subFromFile);
                    epic.setSubtaskList(subList);
                    return subFromFile;
                case "TASK":
                    LocalDate timeFromString = LocalDate.parse(params[5]);
                    String startTime = timeFromString.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    return new Task(params[2], params[4], parsedId, startTime, Integer.parseInt(params[6]));
                case "EPIC":
                    Epic epic1 = new Epic(params[2], params[4], parsedId);
                    epicForInside.put(parsedId, epic1);
                    return epic1;
                }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Недостаточно введенных параметров о задаче. Проверьте содержимое файла.");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.out.println("Неверный формат введенных данных.");
            e.printStackTrace();
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
    public void setStartTime(String startTime, long id) throws ManagerSaveException {
        super.setStartTime(startTime, id);
        save();
    }

    @Override
    public void setDuration(int duration, long id) throws ManagerSaveException {
        super.setDuration(duration, id);
        save();
    }

    @Override
    public void setStatus(String status, long id) throws ManagerSaveException {
        super.setStatus(status, id);
        save();
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    @Override
    public Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic) throws ManagerSaveException {
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
    public Epic createNewEpic(String inputName, String inputDescription, long id) throws ManagerSaveException {
        Epic newEpic = super.createNewEpic(inputName, inputDescription, id);
        save();
        return newEpic;
    }

    @Override
    public Subtask createNewSubtask(String inputName, String inputDescription, long id, Epic epic, String startTime,
                                    int duration) throws ManagerSaveException {
        Subtask newSubtask = super.createNewSubtask(inputName, inputDescription, id, startTime, duration, epic);
        save();
        return newSubtask;
    }

    @Override
    public Task createNewTask(String inputName, String inputDescription, long id, String startTime, int duration)
            throws ManagerSaveException {
        Task newTask = super.createNewTask(inputName, inputDescription, id, startTime, duration);
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