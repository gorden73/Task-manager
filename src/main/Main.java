package main;

import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Manager manager = new Manager();
        Scanner scanner = new Scanner(System.in);
        String inputName;
        String inputDescription;
        String inputAnswer = null;
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
                        Epic epic = manager.createNewEpic(inputName, inputDescription, id);
                        do {
                            System.out.println("Введите название подзадачи");
                            inputName = scanner.nextLine();
                            if (inputName.equals("Всё") && !epic.getSubtaskList().isEmpty()) {
                                break;
                            }
                            System.out.println("Введите краткое описание подзадачи");
                            inputDescription = scanner.nextLine();
                            id++;
                            manager.createNewSubtask(inputName, inputDescription, id, epic);
                            System.out.println("Если хотите закончить, введите Всё");
                        } while (true);
                    } else if (inputAnswer.equals("Нет")) {
                        manager.createNewTask(inputName, inputDescription, id);
                    }
                    break;
                case "2": //получаем список задач
                case "3": //получаем список эпиков
                    printTasks(command, inputId, manager);
                    break;
                case "4":
                    System.out.println("Введите id эпика для просмотра его подзадач");
                    inputId = scanner.nextLong();
                    if (manager.getEpics().containsKey(inputId)) {
                        printTasks(command, inputId, manager);
                    } else {
                        System.out.println("Эпика с таким id нет");
                    }
                    break;
                case "5": //получаем любую задачу по id
                    System.out.println("Введите id");
                    inputId = scanner.nextLong();
                    if ((manager.getTasks().containsKey(inputId) && manager.getEpics().containsKey(inputId))
                        || (manager.getTasks().containsKey(inputId) && manager.getSubtasks().containsKey(inputId))
                        || (manager.getEpics().containsKey(inputId) && manager.getSubtasks().containsKey(inputId))
                        || (manager.getTasks().containsKey(inputId) && manager.getSubtasks().containsKey(inputId)
                        && manager.getEpics().containsKey(inputId))) {
                        System.out.println("К какому типу относится задача?");
                        System.out.println("Введите task, subtask или epic");
                        inputAnswer = scanner.next();
                    }
                    if (manager.getTasks().containsKey(inputId) || inputAnswer.equals("task")) {
                        Task task1 = manager.getTask(inputId);
                        System.out.println(task1);
                    } else if (manager.getEpics().containsKey(inputId) || inputAnswer.equals("epic")) {
                        Epic epic1 = manager.getEpic(inputId);
                        System.out.println(epic1);
                    } else if (manager.getSubtasks().containsKey(inputId) || inputAnswer.equals("subtask")) {
                        Subtask subtask1 = manager.getSubtask(inputId);
                        System.out.println(subtask1);
                        Long epicId = manager.getSubtaskVsEpic().get(inputId);
                        System.out.println(manager.getEpics().get(epicId));
                    } else {
                        System.out.println("Задачи под таким id нет");
                    }
                    break;
                case "6": //изменяем любую задачу по id
                    System.out.println("Введите id задачи для изменения");
                    inputId = scanner.nextLong();
                    scanner.nextLine();//
                    if (manager.getTasks().containsKey(inputId) || manager.getSubtasks().containsKey(inputId)
                        || manager.getEpics().containsKey(inputId)) {
                        System.out.println("Хотите изменить только статус задачи?");
                        System.out.println("Введите NEW, IN_PROGRESS или DONE для задач и подзадач");
                        String answer = scanner.nextLine();
                        if (answer.equals("NEW") || answer.equals("IN_PROGRESS") || answer.equals("DONE")) {
                            if (manager.getTasks().containsKey(inputId)) {
                                Task newTask = manager.getTasks().get(inputId);
                                newTask.setStatus(answer);
                                System.out.println("Статус изменён на " + answer);
                            } else if (manager.getSubtasks().containsKey(inputId)) {
                                Subtask newSubtask = manager.getSubtasks().get(inputId);
                                newSubtask.setStatus(answer);
                                System.out.println("Статус изменён на " + answer);
                                Long epicId = manager.getSubtaskVsEpic().get(inputId);
                                Epic epic = manager.getEpics().get(epicId);
                                epic.setStatus(answer);
                            } else {
                                System.out.println("Нельзя изменять статус у эпиков");
                            }
                        } else {
                            System.out.println("Введите название новой задачи");
                            inputName = scanner.nextLine();
                            System.out.println("Введите краткое описание задачи");
                            inputDescription = scanner.nextLine();
                            if (manager.getTasks().containsKey(inputId)) {
                                Task newTask = manager.getTasks().get(inputId);
                                newTask.setName(inputName);
                                newTask.setDescription(inputDescription);
                                manager.updateTask(inputId, newTask);
                            } else if (manager.getSubtasks().containsKey(inputId)) {
                                Subtask newSubtask = manager.getSubtasks().get(inputId);
                                newSubtask.setName(inputName);
                                newSubtask.setDescription(inputDescription);
                                manager.updateSubtask(inputId, newSubtask);
                            } else if (manager.getEpics().containsKey(inputId)) {
                                Epic newEpic = manager.getEpics().get(inputId);
                                newEpic.setName(inputName);
                                newEpic.setDescription(inputDescription);
                                manager.updateEpic(inputId, newEpic);
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
                    manager.removeTask(inputId);
                    break;
                case "8": //удаляем подзадачу по id
                    System.out.println("Введите id");
                    inputId = scanner.nextLong();
                    manager.removeSubtask(inputId);
                    break;
                case "9": //удаляем эпик по id
                    System.out.println("Введите id");
                    inputId = scanner.nextLong();
                    manager.removeEpic(inputId);
                    break;
                case "10": //удаляем все задачи
                    manager.removeAllTasks(manager.getTasks(), manager.getSubtasks(), manager.getEpics(),
                        manager.getEpicVsSubtask(), manager.getSubtaskVsEpic());
                    System.out.println("Все задачи удалены");
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
            + "0. Выйти из приложения");
    }

    public static void printTasks(String command, Long inputId, Manager manager) { //выводим в консоль списки задач
        switch (command) {
            case "2": // получение всех задач
                if (!manager.getTasks().isEmpty()) {
                    System.out.println(manager.getTasks().values());
                    System.out.println("Это все задачи");
                    break;
                } else {
                    System.out.println("Задач пока нет");
                    break;
                }
            case "3": //получение всех эпиков
                if (!manager.getEpics().isEmpty()) {
                    System.out.println(manager.getEpics().values());
                    System.out.println("Это все эпики");
                    break;
                } else {
                    System.out.println("Эпиков пока нет");
                    break;
                }
            case "4": //получение подзадач по id эпика
                System.out.println(manager.getEpics().get(inputId));
                ArrayList<Subtask> subtaskId = manager.getEpicVsSubtask().get(inputId);
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
