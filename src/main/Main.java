package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();
        Scanner scanner = new Scanner(System.in);
        String inputName;
        String inputDescription;
        String inputAnswer = null;
        String command;
        long inputId = 0;
        long id;

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
                    System.out.println("Введите Id");
                    id = scanner.nextLong();
                    System.out.println("Задача требует разделения?");
                    inputAnswer = scanner.next();
                    scanner.nextLine();
                    if (inputAnswer.equals("Да")) {
                        Epic epic = taskManager.createNewEpic(inputName, inputDescription, id);
                        do {
                            System.out.println("Введите название подзадачи");
                            inputName = scanner.nextLine();
                            if (inputName.equals("Всё") && !epic.getSubtaskList().isEmpty()) {
                                break;
                            }
                            System.out.println("Введите краткое описание подзадачи");
                            inputDescription = scanner.nextLine();
                            id++;
                            taskManager.createNewSubtask(inputName, inputDescription, id, epic);
                            System.out.println("Если хотите закончить, введите Всё");
                        } while (true);
                    } else if (inputAnswer.equals("Нет")) {
                        taskManager.createNewTask(inputName, inputDescription, id);
                    }
                    break;
                case "2": //получаем список задач
                case "3": //получаем список эпиков
                    printTasks(command, inputId, taskManager);
                    break;
                case "4":
                    System.out.println("Введите id эпика для просмотра его подзадач");
                    inputId = scanner.nextLong();
                    if (taskManager.getEpics().containsKey(inputId)) {
                        printTasks(command, inputId, taskManager);
                    } else {
                        System.out.println("Эпика с таким id нет");
                    }
                    break;
                case "5": //получаем любую задачу по id
                    System.out.println("Введите id");
                    inputId = scanner.nextLong();
                    if ((taskManager.getTasks().containsKey(inputId) && taskManager.getEpics().containsKey(inputId))
                        || (taskManager.getTasks().containsKey(inputId) && taskManager.getSubtasks().containsKey(inputId))
                        || (taskManager.getEpics().containsKey(inputId) && taskManager.getSubtasks().containsKey(inputId))
                        || (taskManager.getTasks().containsKey(inputId) && taskManager.getSubtasks().containsKey(inputId)
                        && taskManager.getEpics().containsKey(inputId))) {
                        System.out.println("К какому типу относится задача?");
                        System.out.println("Введите task, subtask или epic");
                        inputAnswer = scanner.next();
                    }
                    if (taskManager.getTasks().containsKey(inputId) || inputAnswer.equals("task")) {
                        Task task1 = taskManager.getTask(inputId);
                        System.out.println(task1);
                    } else if (taskManager.getEpics().containsKey(inputId) || inputAnswer.equals("epic")) {
                        Epic epic1 = taskManager.getEpic(inputId);
                        System.out.println(epic1);
                    } else if (taskManager.getSubtasks().containsKey(inputId) || inputAnswer.equals("subtask")) {
                        Subtask subtask1 = taskManager.getSubtask(inputId);
                        System.out.println(subtask1);
                        Long epicId = taskManager.getSubtaskVsEpic().get(inputId);
                        System.out.println(taskManager.getEpics().get(epicId));
                    } else {
                        System.out.println("Задачи под таким id нет");
                    }
                    break;
                case "6": //изменяем любую задачу по id
                    System.out.println("Введите id задачи для изменения");
                    inputId = scanner.nextLong();
                    scanner.nextLine();//
                    if (taskManager.getTasks().containsKey(inputId) || taskManager.getSubtasks().containsKey(inputId)
                        || taskManager.getEpics().containsKey(inputId)) {
                        System.out.println("Хотите изменить только статус задачи?");
                        System.out.println("Введите NEW, IN_PROGRESS или DONE для задач и подзадач");
                        String answer = scanner.nextLine();
                        if (answer.equals("NEW") || answer.equals("IN_PROGRESS") || answer.equals("DONE")) {
                            if (taskManager.getTasks().containsKey(inputId)) {
                                Task newTask = taskManager.getTasks().get(inputId);
                                newTask.setStatus(answer);
                                System.out.println("Статус изменён на " + answer);
                            } else if (taskManager.getSubtasks().containsKey(inputId)) {
                                Subtask newSubtask = taskManager.getSubtasks().get(inputId);
                                newSubtask.setStatus(answer);
                                System.out.println("Статус изменён на " + answer);
                                Long epicId = taskManager.getSubtaskVsEpic().get(inputId);
                                Epic epic = taskManager.getEpics().get(epicId);
                                epic.setStatus(answer);
                            } else {
                                System.out.println("Нельзя изменять статус у эпиков");
                            }
                        } else {
                            System.out.println("Введите название новой задачи");
                            inputName = scanner.nextLine();
                            System.out.println("Введите краткое описание задачи");
                            inputDescription = scanner.nextLine();
                            if (taskManager.getTasks().containsKey(inputId)) {
                                Task newTask = taskManager.getTasks().get(inputId);
                                newTask.setName(inputName);
                                newTask.setDescription(inputDescription);
                                taskManager.updateTask(inputId, newTask);
                            } else if (taskManager.getSubtasks().containsKey(inputId)) {
                                Subtask newSubtask = taskManager.getSubtasks().get(inputId);
                                newSubtask.setName(inputName);
                                newSubtask.setDescription(inputDescription);
                                taskManager.updateSubtask(inputId, newSubtask);
                            } else if (taskManager.getEpics().containsKey(inputId)) {
                                Epic newEpic = taskManager.getEpics().get(inputId);
                                newEpic.setName(inputName);
                                newEpic.setDescription(inputDescription);
                                taskManager.updateEpic(inputId, newEpic);
                            }
                        }
                        break;
                    } else {
                        System.out.println("Нет задач для обновления");
                        break;
                    }
                case "7": //удаляем задачу по id
                    System.out.println("Введите id");
                    inputId = scanner.nextLong();
                    taskManager.removeTask(inputId);
                    break;
                case "8": //удаляем подзадачу по id
                    System.out.println("Введите id");
                    inputId = scanner.nextLong();
                    taskManager.removeSubtask(inputId);
                    break;
                case "9": //удаляем эпик по id
                    System.out.println("Введите id");
                    inputId = scanner.nextLong();
                    taskManager.removeEpic(inputId);
                    break;
                case "10": //удаляем все задачи
                    taskManager.removeAllTasks(taskManager.getTasks(), taskManager.getSubtasks(), taskManager.getEpics(),
                        taskManager.getEpicVsSubtask(), taskManager.getSubtaskVsEpic());
                    System.out.println("Все задачи удалены");
                    break;
                case "11" : // история просмотров
                    if (taskManager.getListOfHistory().isEmpty()) {
                        System.out.println("История просмотров пустая");
                    }
                    for(Task task : taskManager.history()) {
                        System.out.println(task);
                    }
                    break;
                case "0":
                    System.out.println("Выход" + "\n" + "До свидания!");
                    break;
            }
        } while (!command.equals("0")); //выход
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
            + "8. Удалить подзадачу по id" + "\n"
            + "9. Удалить эпик по id" + "\n"
            + "10. Удалить все задачи" + "\n"
                + "11. Показать историю просмотров" + "\n"
            + "0. Выйти из приложения");
    }

    public static void printTasks(String command, Long inputId, TaskManager taskManager) { //выводим в консоль списки задач
        switch (command) {
            case "2": // получение всех задач
                if (!taskManager.getTasks().isEmpty()) {
                    System.out.println(taskManager.getTasks().values());
                    System.out.println("Это все задачи");
                    break;
                } else {
                    System.out.println("Задач пока нет");
                    break;
                }
            case "3": //получение всех эпиков
                if (!taskManager.getEpics().isEmpty()) {
                    System.out.println(taskManager.getEpics().values());
                    System.out.println("Это все эпики");
                    break;
                } else {
                    System.out.println("Эпиков пока нет");
                    break;
                }
            case "4": //получение подзадач по id эпика
                System.out.println(taskManager.getEpics().get(inputId));
                ArrayList<Subtask> subtaskId = taskManager.getEpicVsSubtask().get(inputId);
                for (Subtask sub : subtaskId) {
                    try {
                        if (sub != null) {
                            System.out.println(sub);
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
}
