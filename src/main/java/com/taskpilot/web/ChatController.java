package com.taskpilot.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskpilot.agent.Agent;
import com.taskpilot.agent.AgentResult;
import com.taskpilot.agent.ConversationStore;
import com.taskpilot.agent.ToolCall;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api")
public class ChatController {

    public record ChatRequest(
            @Size(max = 100) String sessionId,
            @NotBlank @Size(max = 4000) String message) {
    }

    public record ChatResponse(String sessionId, String reply, int steps, List<ToolCall> toolCalls) {
    }

    private final Agent agent;
    private final ConversationStore store;

    public ChatController(Agent agent, ConversationStore store) {
        this.agent = agent;
        this.store = store;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        String sessionId = (request.sessionId() == null || request.sessionId().isBlank())
                ? UUID.randomUUID().toString()
                : request.sessionId();
        AgentResult result = agent.run(sessionId, request.message());
        return new ChatResponse(sessionId, result.reply(), result.steps(), result.toolCalls());
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> reset(@PathVariable String sessionId) {
        return store.clear(sessionId) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
