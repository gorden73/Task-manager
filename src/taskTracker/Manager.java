package taskTracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Manager {
    static HashMap<Long, Task> tasks = new HashMap<>();
    static HashMap<Long, Subtask> subtasks = new HashMap<>();
    static HashMap<Long, Epic> epics = new HashMap<>();
    static HashMap<Long, ArrayList<Long>> epicVsSubtask = new HashMap<>();
    static HashMap<Long, Long> subtaskVsEpic = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String inputName;
        String inputDescription;
        String inputAnswer;
        String command;
        long inputId = 0;
        long id = 0;

        do {
            printMenu();
            command = scanner.next();
            scanner.nextLine();
            switch (command) {
                case "1": //добавляем новую задачу
                    System.out.println("Введите название задачи");
                    inputName = scanner.nextLine();
                    System.out.println("Введите краткое описание задачи");
                    inputDescription = scanner.nextLine();
                    id++;
                    System.out.println("Задача требует разделения?");
                    inputAnswer = scanner.next();
                    scanner.nextLine();
                    if (inputAnswer.equals("Да")) {
                        Epic epic = createNewEpic(inputName, inputDescription, id);
                        ArrayList<Long> subtaskList = new ArrayList<>();
                        do {
                            System.out.println("Введите название подзадачи");
                            inputName = scanner.nextLine();
                            if (inputName.equals("Всё") && !subtaskList.isEmpty()) {
                                break;
                            }
                            System.out.println("Введите краткое описание подзадачи");
                            inputDescription = scanner.nextLine();
                            id++;
                            createNewSubtask(inputName, inputDescription, id);
                            subtaskList.add(id);
                            epicVsSubtask.put(epic.getId(), subtaskList);
                            subtaskVsEpic.put(id, epic.getId());
                            System.out.println("Если хотите закончить, введите Всё");
                        } while (true);
                    } else if (inputAnswer.equals("Нет")) {
                        createNewTask(inputName, inputDescription, id);
                    }
                    break;
                case "2": //получаем список задач
                case "3": //получаем список эпиков
                    printTasks(command, inputId);
                    break;
                case "4":
                    System.out.println("Введите id эпика для просмотра его подзадач");
                    inputId = scanner.nextLong();
                    if (epics.containsKey(inputId)) {
                        printTasks(command, inputId);
                    } else {
                        System.out.println("Эпика с таким id нет");
                    }
                    break;
                case "5": //получаем любую задачу по id
                    System.out.println("Введите id");
                    inputId = scanner.nextLong();
                    getSomeTask(inputId);
                    break;
                case "6": //изменяем любую задачу по id
                    System.out.println("Введите id задачи для изменения");
                    inputId = scanner.nextLong();
                    scanner.nextLine();//
                    if (tasks.containsKey(inputId) || subtasks.containsKey(inputId)
                            || epics.containsKey(inputId)) {
                        System.out.println("Хотите изменить только статус задачи?");
                        System.out.println("Введите NEW, IN_PROGRESS или DONE для задач и " +
                                        "подзадач");
                        String answer = scanner.nextLine();
                        if (answer.equals("NEW") || answer.equals("IN_PROGRESS")
                                || answer.equals("DONE")) {
                            if (tasks.containsKey(inputId)) {
                                Task newTask = tasks.get(inputId);
                                newTask.setStatus(answer);
                                System.out.println("Статус изменён на " + answer);
                            } else if (subtasks.containsKey(inputId)) {
                                Subtask newSubtask = subtasks.get(inputId);
                                newSubtask.setStatus(answer);
                                System.out.println("Статус изменён на " + answer);
                                Long epicId = subtaskVsEpic.get(inputId);
                                Epic epic = epics.get(epicId);
                                if (answer.equals("IN_PROGRESS") || answer.equals("DONE")) {
                                    epic.setStatus("IN_PROGRESS");
                                    if (answer.equals("DONE")) {
                                        int count = 0;
                                        for (Subtask sub : subtasks.values()) {
                                            if (sub.getStatus().equals(answer)) {
                                                count++;
                                            }
                                        }
                                        if (count == subtasks.size()) {
                                            epic.setStatus(answer);
                                        }
                                    }
                                }
                            } else {
                                System.out.println("Нельзя изменять статус у эпиков");
                            }
                        } else {
                            System.out.println("Введите название новой задачи");
                            inputName = scanner.nextLine();
                            System.out.println("Введите краткое описание задачи");
                            inputDescription = scanner.nextLine();
                            if (tasks.containsKey(inputId)) {
                                Task newTask = tasks.get(inputId);
                                newTask.setName(inputName);
                                newTask.setDescription(inputDescription);
                                updateTask(tasks, inputId, newTask);
                            } else if (subtasks.containsKey(inputId)) {
                                Subtask newSubtask = subtasks.get(inputId);
                                newSubtask.setName(inputName);
                                newSubtask.setDescription(inputDescription);
                                updateSubtask(subtasks, inputId, newSubtask);
                            } else if (epics.containsKey(inputId)) {
                                Epic newEpic = epics.get(inputId);
                                newEpic.setName(inputName);
                                newEpic.setDescription(inputDescription);
                                updateEpic(epics, inputId, newEpic);
                            }
                        }
                        break;
                    } else {
                        System.out.println("Нет задач для обновления");
                        break;
                    }
                case "7": //удаляем любую задачу по id
                    System.out.println("Введите id");
                    inputId = scanner.nextLong();
                    removeSomeTask(inputId);
                    System.out.println("Задача удалена");
                    break;
                case "8": //удаляем все задачи
                    removeAllTasks(tasks, subtasks, epics, epicVsSubtask, subtaskVsEpic);
                    System.out.println("Все задачи удалены");
                    break;
                case "9":
                    System.out.println("Выход" + "\n" + "До свидания!");
                    break;
            }
        } while (!command.equals("9")); //выход
    }

    public static Epic createNewEpic(String inputName, String inputDescription, long id) {
        Epic epic = new Epic(inputName, inputDescription, id);
        epics.put(id, epic);
        System.out.println("Задача добавлена");
        return epic;
    }

    public static Subtask createNewSubtask(String inputName, String inputDescription, long id) {
        Subtask subtask = new Subtask(inputName, inputDescription, id);
        subtasks.put(id, subtask);
        System.out.println("Задача добавлена");
        return subtask;
    }

    public static Task createNewTask(String inputName, String inputDescription, long id) {
        Task task = new Task(inputName, inputDescription, id);
        tasks.put(id, task);
        System.out.println("Задача добавлена");
        return task;
    }

    public static void printTasks(String command, Long inputId) { //выводим в консоль списки задач
        switch (command) {
            case "2": // получение всех задач
                if (!tasks.isEmpty()) {
                    System.out.println(tasks.values());
                    System.out.println("Это все задачи");
                    break;
                } else {
                    System.out.println("Задач пока нет");
                    break;
                }
            case "3": //получение всех эпиков
                if (!epics.isEmpty()) {
                    System.out.println(epics.values());
                    System.out.println("Это все эпики");
                    break;
                } else {
                    System.out.println("Эпиков пока нет");
                    break;
                }
            case "4": //получение подзадач по id эпика
                    System.out.println(epics.get(inputId));
                    ArrayList<Long> subtaskId = epicVsSubtask.get(inputId);
                    for (Long id : subtaskId) {
                        try {
                            if (subtasks.get(id)!= null) {
                                System.out.println(subtasks.get(id)); //вроде работает как надо
                            }
                        } catch (NullPointerException e) {
                        }
                    }
                    System.out.println("Это все подзадачи эпика");
                    break;
            default:
                System.out.println("--------------------------------------");
        }
    }

    public static void getSomeTask(long inputId) { //получаем любую задачу по id
        if (tasks.containsKey(inputId)) {
            System.out.println(tasks.get(inputId));
        } else if (epics.containsKey(inputId)) {
            System.out.println(epics.get(inputId));
        } else if (subtasks.containsKey(inputId)) {
            System.out.println(subtasks.get(inputId));
            Long epicId = subtaskVsEpic.get(inputId);
            System.out.println(epics.get(epicId));
        } else {
            System.out.println("Задачи под таким id нет");
        }
    }

    public static void updateTask(HashMap<Long, Task> tasks, long inputId, Task task) {
        tasks.put(inputId, task);
    }

    public static void updateSubtask(HashMap<Long, Subtask> subtasks, long inputId,
                                     Subtask subtask) {
        subtasks.put(inputId, subtask);
    }

    public static void updateEpic(HashMap<Long, Epic> epics, long inputId, Epic epic) {
        epics.put(inputId, epic);
    }

    public static void removeSomeTask(long inputId) { //удаляем любую задачу по id
        if (tasks.containsKey(inputId)) {
            tasks.remove(inputId);
        } else if (epics.containsKey(inputId)) {
            epics.remove(inputId);
            ArrayList<Long> subtasks1 = epicVsSubtask.get(inputId);
            for (Long id : subtasks1) {
                subtasks.remove(id);
            }
        } else if (subtasks.containsKey(inputId)) {
            subtasks.remove(inputId);
            Long epicId = subtaskVsEpic.get(inputId);
            ArrayList<Long> subtasks1 = epicVsSubtask.get(epicId);
            subtasks1.remove(inputId);
            if (subtasks1.isEmpty()) {
                epics.remove(subtaskVsEpic.get(inputId));
            }
        } else {
            System.out.println("Задачи под таким id нет");
        }
    }

    public static void removeAllTasks(HashMap<Long, Task> tasks, HashMap<Long, Subtask> subtasks,
                                      HashMap<Long, Epic> epics,
                                      HashMap<Long, ArrayList<Long>> epicVsSubtask,
                                      HashMap<Long, Long> subtaskVsEpic) {
        tasks.clear();
        subtasks.clear();
        epics.clear();
        epicVsSubtask.clear();
        subtaskVsEpic.clear();
    }

    public static void printMenu () {
        System.out.println("Рады приветствовать Вас в нашем приложении - Планировщик задач!");
        System.out.println("Чего желаете?");
        System.out.println("1. Добавить новую задачу" + "\n"
                + "2. Получить список задач" + "\n"
                + "3. Получить список эпиков" + "\n"
                + "4. Получить список подзадач по id эпика" + "\n"
                + "5. Получить задачу по id" + "\n"
                + "6. Обновить задачу по id" + "\n"
                + "7. Удалить задачу по id" + "\n"
                + "8. Удалить все задачи" + "\n"
                + "9. Выйти из приложения");
    }
}
