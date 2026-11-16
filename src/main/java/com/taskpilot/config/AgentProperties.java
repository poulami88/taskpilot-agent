package com.taskpilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "agent")
public record AgentProperties(
        @DefaultValue("8") int maxSteps,
        @DefaultValue(DEFAULT_SYSTEM_PROMPT) String systemPrompt) {

    public static final String DEFAULT_SYSTEM_PROMPT = """
            You are TaskPilot, a concise and careful assistant with access to tools.
            Use the calculator for any arithmetic instead of computing in your head.
            Use the time tool for date or time questions.
            Use the notes tools when the user asks you to remember or recall something.
            If a tool returns an error, explain it briefly and try a corrected call if sensible.
            """;
}
