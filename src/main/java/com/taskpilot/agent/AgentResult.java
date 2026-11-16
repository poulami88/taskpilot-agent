package com.taskpilot.agent;

import java.util.List;

public record AgentResult(String reply, int steps, List<ToolCall> toolCalls) {
}
