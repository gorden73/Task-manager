package main.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import main.HTTPTaskManager;
import tasktracker.Epic;
import tasktracker.Subtask;

import java.io.IOException;
import java.util.HashMap;

public class SubtaskAdapter extends TypeAdapter<Subtask> {
    HashMap<Long, Epic> epics;

    public SubtaskAdapter(HashMap<Long, Epic> epics) {
        this.epics = epics;
    }

    @Override
    public void write(JsonWriter jsonWriter, Subtask subtask) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("Class").value("Subtask");
        jsonWriter.name("name").value(subtask.getName());
        jsonWriter.name("description").value(subtask.getDescription());
        jsonWriter.name("id").value(subtask.getId());
        jsonWriter.name("status").value(subtask.getStatus().toString());
        jsonWriter.name("startTime").value(subtask.getStartTime().toString());
        jsonWriter.name("duration").value(subtask.getDuration().toDays());
        jsonWriter.name("endTime").value(subtask.getEndTime().toString());
        jsonWriter.name("epicId").value(subtask.getEpic().getId());
        jsonWriter.endObject();
    }

    @Override
    public Subtask read(JsonReader jsonReader) {
        JsonElement jsonElement = JsonParser.parseReader(jsonReader);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Subtask subtask;
        Epic epic = epics.get(jsonObject.get("epicId").getAsLong());
        if (jsonObject.get("startTime") == null) {
            subtask = new Subtask(jsonObject.get("name").getAsString(),
                    jsonObject.get("description").getAsString(),
                    jsonObject.get("id").getAsLong(),
                    epic);
        } else {
            subtask = new Subtask(jsonObject.get("name").getAsString(),
                    jsonObject.get("description").getAsString(),
                    jsonObject.get("id").getAsLong(),
                    jsonObject.get("startTime").getAsString(),
                    jsonObject.get("duration").getAsInt(),
                    epic);
        }
        if (jsonObject.get("status") != null) {
            subtask.setStatus(jsonObject.get("status").getAsString());
        }
        return subtask;
    }
}
