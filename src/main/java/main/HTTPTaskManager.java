package main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.adapters.EpicAdapter;
import main.adapters.SubtaskAdapter;
import main.adapters.TaskAdapter;
import tasktracker.Epic;
import tasktracker.Subtask;
import tasktracker.Task;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class HTTPTaskManager extends FileBackedTasksManager {
    private KVTaskClient client;
    Gson gson;
    private TaskAdapter taskAdapter;
    private SubtaskAdapter subtaskAdapter;
    private EpicAdapter epicAdapter;

    public HTTPTaskManager() throws IOException, InterruptedException {
        client = new KVTaskClient(URI.create("http://localhost:8078"));
        taskAdapter = new TaskAdapter();
        subtaskAdapter = new SubtaskAdapter(getEpics());
        epicAdapter = new EpicAdapter();
        gson = new GsonBuilder()
                .registerTypeAdapter(Task.class, taskAdapter)
                .registerTypeAdapter(Subtask.class, subtaskAdapter)
                .registerTypeAdapter(Epic.class, epicAdapter)
                .create();
    }

    public String load(String key) throws IOException, InterruptedException {
        return client.load(key);
    }

    @Override
    public void save() {
        try {
            super.save();
            client.put("tasks", gson.toJson(getTasks()));
            client.put("epics", gson.toJson(getEpics()));
            client.put("subtasks", gson.toJson(getSubtasks()));
            List<Task> history = getHistory();
            ArrayList<Long> historyId = new ArrayList<>();
            for (Task task : history) {
                historyId.add(task.getId());
            }
            client.put("history", gson.toJson(historyId));
        } catch (IOException | InterruptedException | ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    public KVTaskClient getClient() {
        return client;
    }

    public Gson getGson() {
        return gson;
    }

    public TaskAdapter getTaskAdapter() {
        return taskAdapter;
    }

    public SubtaskAdapter getSubtaskAdapter() {
        return subtaskAdapter;
    }

    public EpicAdapter getEpicAdapter() {
        return epicAdapter;
    }
}
