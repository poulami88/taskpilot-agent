package com.taskpilot.tools;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class CalculatorTool implements Tool {

    @Override
    public String name() {
        return "calculator";
    }

    @Override
    public String description() {
        return "Evaluates an arithmetic expression. Supports + - * / % ^, parentheses and decimals. "
                + "Example: (12.5 + 7) * 3";
    }

    @Override
    public ObjectNode inputSchema() {
        return Tool.objectSchema(Map.of("expression", "The arithmetic expression to evaluate"),
                List.of("expression"));
    }

    @Override
    public String execute(JsonNode input) {
        return ExpressionParser.evaluate(input.path("expression").asText(null));
    }
}
