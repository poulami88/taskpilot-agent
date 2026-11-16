package com.taskpilot.agent;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.taskpilot.config.AgentProperties;
import com.taskpilot.llm.LlmClient;
import com.taskpilot.tools.ToolOutcome;
import com.taskpilot.tools.ToolRegistry;

/**
 * The agent loop: send the conversation to the model; if it asks to use tools, run them,
 * feed the results back, and repeat until the model produces a final answer.
 */
@Service
public class Agent {

    private final LlmClient llm;
    private final ToolRegistry tools;
    private final ConversationStore store;
    private final AgentProperties props;
    private final ObjectMapper mapper;

    public Agent(LlmClient llm, ToolRegistry tools, ConversationStore store,
                 AgentProperties props, ObjectMapper mapper) {
        this.llm = llm;
        this.tools = tools;
        this.store = store;
        this.props = props;
        this.mapper = mapper;
    }

    public AgentResult run(String sessionId, String userText) {
        List<JsonNode> history = store.history(sessionId);
        synchronized (history) {
            int startSize = history.size();
            try {
                return loop(history, userText);
            } catch (RuntimeException e) {
                // Never leave a dangling tool_use without a tool_result: it would break the session.
                while (history.size() > startSize) {
                    history.remove(history.size() - 1);
                }
                throw e;
            }
        }
    }

    private AgentResult loop(List<JsonNode> history, String userText) {
        List<ToolCall> trace = new ArrayList<>();
        history.add(textMessage("user", userText));

        for (int step = 1; step <= props.maxSteps(); step++) {
            JsonNode response = llm.createMessage(props.systemPrompt(), history, tools.definitions());
            JsonNode content = response.path("content");
            history.add(blocksMessage("assistant", content));

            if (!"tool_use".equals(response.path("stop_reason").asText())) {
                return new AgentResult(extractText(content), step, trace);
            }

            ArrayNode results = mapper.createArrayNode();
            for (JsonNode block : content) {
                if (!"tool_use".equals(block.path("type").asText())) {
                    continue;
                }
                String name = block.path("name").asText();
                JsonNode input = block.path("input");
                ToolOutcome outcome = tools.execute(name, input);
                trace.add(new ToolCall(name, input, outcome.output(), outcome.error()));
                results.add(toolResult(block.path("id").asText(), outcome));
            }
            history.add(blocksMessage("user", results));
        }
        throw new AgentException("Agent stopped after " + props.maxSteps() + " steps without a final answer");
    }

    private ObjectNode textMessage(String role, String text) {
        ObjectNode message = mapper.createObjectNode();
        message.put("role", role);
        message.put("content", text);
        return message;
    }

    private ObjectNode blocksMessage(String role, JsonNode blocks) {
        ObjectNode message = mapper.createObjectNode();
        message.put("role", role);
        message.set("content", blocks);
        return message;
    }

    private ObjectNode toolResult(String toolUseId, ToolOutcome outcome) {
        ObjectNode block = mapper.createObjectNode();
        block.put("type", "tool_result");
        block.put("tool_use_id", toolUseId);
        block.put("content", outcome.output());
        if (outcome.error()) {
            block.put("is_error", true);
        }
        return block;
    }

    private String extractText(JsonNode content) {
        StringBuilder text = new StringBuilder();
        for (JsonNode block : content) {
            if ("text".equals(block.path("type").asText())) {
                if (text.length() > 0) {
                    text.append('\n');
                }
                text.append(block.path("text").asText());
            }
        }
        return text.toString();
    }
}
