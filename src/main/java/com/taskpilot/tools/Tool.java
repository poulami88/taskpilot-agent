package com.taskpilot.tools;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** A capability the agent can use. Add a new @Component implementing this to extend the agent. */
public interface Tool {

    String name();

    String description();

    /** JSON Schema describing the tool's input. */
    ObjectNode inputSchema();

    /** Runs the tool. Throw an exception to report an error back to the model. */
    String execute(JsonNode input) throws Exception;

    /** Helper to build a JSON Schema object whose properties are all strings. */
    static ObjectNode objectSchema(Map<String, String> stringProperties, List<String> required) {
        ObjectNode schema = JsonNodeFactory.instance.objectNode();
        schema.put("type", "object");
        ObjectNode properties = schema.putObject("properties");
        stringProperties.forEach((name, description) -> {
            ObjectNode property = properties.putObject(name);
            property.put("type", "string");
            property.put("description", description);
        });
        ArrayNode requiredArray = schema.putArray("required");
        required.forEach(requiredArray::add);
        return schema;
    }
}
