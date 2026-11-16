package com.taskpilot.tools;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class SaveNoteTool implements Tool {

    private static final int MAX_LENGTH = 500;

    private final NoteStore store;

    public SaveNoteTool(NoteStore store) {
        this.store = store;
    }

    @Override
    public String name() {
        return "save_note";
    }

    @Override
    public String description() {
        return "Saves a short note so it can be recalled later.";
    }

    @Override
    public ObjectNode inputSchema() {
        return Tool.objectSchema(Map.of("text", "The note text (max " + MAX_LENGTH + " characters)"),
                List.of("text"));
    }

    @Override
    public String execute(JsonNode input) {
        String text = input.path("text").asText("").trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Note text is empty");
        }
        if (text.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Note is too long (max " + MAX_LENGTH + " characters)");
        }
        NoteStore.Note note = store.add(text);
        return "Saved note #" + note.id();
    }
}
