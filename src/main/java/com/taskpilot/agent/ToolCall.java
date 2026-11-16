package com.taskpilot.agent;

import com.fasterxml.jackson.databind.JsonNode;

/** One tool invocation made by the agent, kept for transparency in API responses. */
public record ToolCall(String tool, JsonNode input, String output, boolean error) {
}
