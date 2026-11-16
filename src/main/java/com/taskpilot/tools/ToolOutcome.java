package com.taskpilot.tools;

public record ToolOutcome(String output, boolean error) {

    public static ToolOutcome ok(String output) {
        return new ToolOutcome(output, false);
    }

    public static ToolOutcome error(String message) {
        return new ToolOutcome(message, true);
    }
}
