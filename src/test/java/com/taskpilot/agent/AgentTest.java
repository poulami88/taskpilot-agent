package com.taskpilot.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.taskpilot.config.AgentProperties;
import com.taskpilot.llm.LlmClient;
import com.taskpilot.llm.LlmException;
import com.taskpilot.tools.CalculatorTool;
import com.taskpilot.tools.ToolRegistry;

class AgentTest {

    private static final String TOOL_USE = """
            {"stop_reason":"tool_use","content":[
              {"type":"text","text":"Let me calculate."},
              {"type":"tool_use","id":"toolu_1","name":"%s","input":%s}]}""";
    private static final String FINAL = """
            {"stop_reason":"end_turn","content":[{"type":"text","text":"15 x 23 = 345"}]}""";

    private final ObjectMapper mapper = new ObjectMapper();
    private final ConversationStore store = new ConversationStore();

    /** Plays back scripted responses instead of calling a real model. */
    private class ScriptedLlm implements LlmClient {
        private final Queue<String> responses = new ArrayDeque<>();

        ScriptedLlm(String... responses) {
            this.responses.addAll(List.of(responses));
        }

        @Override
        public JsonNode createMessage(String system, List<JsonNode> messages, List<ObjectNode> tools) {
            String next = responses.poll();
            if (next == null) {
                throw new LlmException("no more scripted responses");
            }
            try {
                return mapper.readTree(next);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private Agent agent(LlmClient llm, int maxSteps) {
        return new Agent(llm, new ToolRegistry(List.of(new CalculatorTool())), store,
                new AgentProperties(maxSteps, "system"), mapper);
    }

    @Test
    void runsToolThenReturnsFinalAnswer() {
        Agent agent = agent(new ScriptedLlm(
                TOOL_USE.formatted("calculator", "{\"expression\":\"15*23\"}"), FINAL), 5);

        AgentResult result = agent.run("s1", "What is 15 x 23?");

        assertEquals("15 x 23 = 345", result.reply());
        assertEquals(2, result.steps());
        assertEquals(1, result.toolCalls().size());
        assertEquals("345", result.toolCalls().get(0).output());
        // user, assistant(tool_use), user(tool_result), assistant(final)
        assertEquals(4, store.history("s1").size());
    }

    @Test
    void reportsUnknownToolToModelAndContinues() {
        Agent agent = agent(new ScriptedLlm(TOOL_USE.formatted("teleport", "{}"), FINAL), 5);

        AgentResult result = agent.run("s2", "hi");

        assertTrue(result.toolCalls().get(0).error());
        assertEquals("15 x 23 = 345", result.reply());
    }

    @Test
    void stopsAfterMaxStepsAndRollsBackHistory() {
        String loop = TOOL_USE.formatted("calculator", "{\"expression\":\"1+1\"}");
        Agent agent = agent(new ScriptedLlm(loop, loop, loop), 2);

        assertThrows(AgentException.class, () -> agent.run("s3", "loop forever"));
        assertEquals(0, store.history("s3").size());
    }

    @Test
    void rollsBackHistoryWhenModelCallFails() {
        Agent agent = agent(new ScriptedLlm(), 3);

        assertThrows(LlmException.class, () -> agent.run("s4", "hello"));
        assertEquals(0, store.history("s4").size());
    }
}
