package com.taskpilot.llm;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.taskpilot.config.AnthropicProperties;

/** Minimal client for the Anthropic Messages API (https://docs.anthropic.com). */
@Component
public class AnthropicClient implements LlmClient {

    private static final String API_VERSION = "2023-06-01";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final AnthropicProperties props;
    private final ObjectMapper mapper;

    public AnthropicClient(AnthropicProperties props, ObjectMapper mapper) {
        this.props = props;
        this.mapper = mapper;
    }

    @Override
    public JsonNode createMessage(String system, List<JsonNode> messages, List<ObjectNode> tools) {
        if (props.apiKey() == null || props.apiKey().isBlank()) {
            throw new LlmException("ANTHROPIC_API_KEY is not set");
        }

        ObjectNode body = mapper.createObjectNode();
        body.put("model", props.model());
        body.put("max_tokens", props.maxTokens());
        body.put("system", system);
        ArrayNode messageArray = body.putArray("messages");
        messages.forEach(messageArray::add);
        if (!tools.isEmpty()) {
            ArrayNode toolArray = body.putArray("tools");
            tools.forEach(toolArray::add);
        }

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(props.baseUrl() + "/v1/messages"))
                    .timeout(Duration.ofSeconds(60))
                    .header("content-type", "application/json")
                    .header("x-api-key", props.apiKey())
                    .header("anthropic-version", API_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new LlmException("Anthropic API returned HTTP " + response.statusCode()
                        + ": " + response.body());
            }
            return mapper.readTree(response.body());
        } catch (JsonProcessingException e) {
            throw new LlmException("Could not process JSON for the Anthropic API", e);
        } catch (IOException e) {
            throw new LlmException("Network error calling the Anthropic API", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmException("Interrupted while calling the Anthropic API", e);
        }
    }
}
