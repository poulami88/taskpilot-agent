package com.taskpilot.tools;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class ListNotesTool implements Tool {

    private final NoteStore store;

    public ListNotesTool(NoteStore store) {
        this.store = store;
    }

    @Override
    public String name() {
        return "list_notes";
    }

    @Override
    public String description() {
        return "Lists all saved notes.";
    }

    @Override
    public ObjectNode inputSchema() {
        return Tool.objectSchema(Map.of(), List.of());
    }

    @Override
    public String execute(JsonNode input) {
        List<NoteStore.Note> notes = store.all();
        if (notes.isEmpty()) {
            return "No notes saved yet.";
        }
        return notes.stream()
                .map(n -> "#" + n.id() + " (" + n.createdAt() + "): " + n.text())
                .collect(Collectors.joining("\n"));
    }
}
