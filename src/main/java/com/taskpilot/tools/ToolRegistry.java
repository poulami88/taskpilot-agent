package com.taskpilot.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Discovers all Tool beans, exposes their definitions to the model, and dispatches calls. */
@Component
public class ToolRegistry {

    private final Map<String, Tool> byName = new LinkedHashMap<>();

    public ToolRegistry(List<Tool> tools) {
        for (Tool tool : tools) {
            if (byName.put(tool.name(), tool) != null) {
                throw new IllegalStateException("Duplicate tool name: " + tool.name());
            }
        }
    }

    public List<ObjectNode> definitions() {
        List<ObjectNode> definitions = new ArrayList<>();
        for (Tool tool : byName.values()) {
            ObjectNode definition = JsonNodeFactory.instance.objectNode();
            definition.put("name", tool.name());
            definition.put("description", tool.description());
            definition.set("input_schema", tool.inputSchema());
            definitions.add(definition);
        }
        return definitions;
    }

    public ToolOutcome execute(String name, JsonNode input) {
        Tool tool = byName.get(name);
        if (tool == null) {
            return ToolOutcome.error("Unknown tool: " + name);
        }
        try {
            return ToolOutcome.ok(tool.execute(input));
        } catch (Exception e) {
            return ToolOutcome.error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
