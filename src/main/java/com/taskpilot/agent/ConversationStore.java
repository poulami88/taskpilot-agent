package com.taskpilot.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

/** In-memory conversation history per session. Replace with a database for persistence. */
@Component
public class ConversationStore {

    private final Map<String, List<JsonNode>> sessions = new ConcurrentHashMap<>();

    public List<JsonNode> history(String sessionId) {
        return sessions.computeIfAbsent(sessionId, id -> new ArrayList<>());
    }

    public boolean clear(String sessionId) {
        return sessions.remove(sessionId) != null;
    }
}
