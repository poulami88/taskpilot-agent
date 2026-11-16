package com.taskpilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "anthropic")
public record AnthropicProperties(
        String apiKey,
        @DefaultValue("claude-sonnet-5-5") String model,
        @DefaultValue("1024") int maxTokens,
        @DefaultValue("https://api.anthropic.com") String baseUrl) {
}
