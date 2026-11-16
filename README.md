# TaskPilot Agent

A tool-using AI agent built with **Java 17, Spring Boot 3 and the Claude API**.
The agent runs a loop: the model decides which tools to call, the app executes them,
results go back to the model, and this repeats until a final answer is produced.

## Architecture

```
POST /api/chat --> ChatController --> Agent (loop) --> LlmClient (AnthropicClient)
                                         |
                                         +--> ToolRegistry --> Tool beans
                                              (calculator, current_time, save_note, list_notes)
ConversationStore keeps per-session history in memory.
```

- `agent/Agent`: the agent loop, step limit, and history rollback on failure
- `llm/LlmClient`: provider interface; `AnthropicClient` calls the Messages API
- `tools/`: each tool is a Spring `@Component` implementing `Tool`; they are auto-registered
- `tools/ExpressionParser`: safe calculator (no `eval`)

## Run

```bash
export ANTHROPIC_API_KEY=your_key_here
mvn spring-boot:run
```

```bash
curl -s localhost:8080/api/chat -H 'content-type: application/json' \
  -d '{"message":"What is (15*23)+7, and what time is it in Asia/Kolkata?"}'
```

Reuse the returned `sessionId` to continue a conversation. Reset it with
`DELETE /api/sessions/{sessionId}`.

## Test

```bash
mvn test
```

Tests use a scripted fake LLM, so they need no API key or network.

## Docker

```bash
docker build -t taskpilot-agent .
docker run -p 8080:8080 -e ANTHROPIC_API_KEY=your_key_here taskpilot-agent
```

## Ideas to extend it

1. Persist sessions and notes in a database (Spring Data JPA)
2. Stream responses to the client (SSE)
3. Add a web search or file-reading tool, with strict input validation
4. Trim or summarize long histories to control token usage
5. Add rate limiting and API authentication
6. Add a small React chat UI
