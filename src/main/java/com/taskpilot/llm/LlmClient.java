package com.taskpilot.llm;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Abstraction over the LLM provider so the agent loop can be tested without network access. */
public interface LlmClient {

    /**
     * Sends the conversation to the model and returns the raw response JSON
     * (must contain "content" and "stop_reason").
     */
    JsonNode createMessage(String system, List<JsonNode> messages, List<ObjectNode> tools);
}
