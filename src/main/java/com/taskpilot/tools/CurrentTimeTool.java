package com.taskpilot.tools;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class CurrentTimeTool implements Tool {

    private final Clock clock;

    public CurrentTimeTool(Clock clock) {
        this.clock = clock;
    }

    @Override
    public String name() {
        return "current_time";
    }

    @Override
    public String description() {
        return "Returns the current date and time in the given IANA timezone (default UTC).";
    }

    @Override
    public ObjectNode inputSchema() {
        return Tool.objectSchema(
                Map.of("timezone", "IANA timezone id such as Asia/Kolkata or America/New_York"),
                List.of());
    }

    @Override
    public String execute(JsonNode input) {
        String timezone = input.path("timezone").asText("UTC");
        ZonedDateTime now = ZonedDateTime.now(clock.withZone(ZoneId.of(timezone)));
        return now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + " (" + now.getZone() + ")";
    }
}
