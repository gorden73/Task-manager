package main.adapters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import tasktracker.Epic;
import tasktracker.Subtask;

import java.io.IOException;

public class EpicAdapter extends TypeAdapter<Epic> {
    @Override
    public void write(JsonWriter jsonWriter, Epic epic) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("Class").value("Epic");
        jsonWriter.name("name").value(epic.getName());
        jsonWriter.name("description").value(epic.getDescription());
        jsonWriter.name("id").value(epic.getId());
        jsonWriter.name("status").value(epic.getStatus().toString());
        jsonWriter.name("startTime").value(epic.getStartTime().toString());
        jsonWriter.name("duration").value(epic.getDuration().toDays());
        jsonWriter.name("endTime").value(epic.getEndTime().toString());
        jsonWriter.name("subtaskList");
        jsonWriter.beginArray();
        for (Subtask sub : epic.getSubtaskList()) {
            jsonWriter.value(sub.getId());
        }
        jsonWriter.endArray();
        jsonWriter.endObject();
    }

    @Override
    public Epic read(JsonReader jsonReader) {
        JsonElement jsonElement = JsonParser.parseReader(jsonReader);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Epic epic;
        epic = new Epic(jsonObject.get("name").getAsString(),
                jsonObject.get("description").getAsString(),
                jsonObject.get("id").getAsLong());
        return epic;
    }
}
