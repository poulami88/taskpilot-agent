package com.taskpilot.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.taskpilot.agent.AgentException;
import com.taskpilot.llm.LlmException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(LlmException.class)
    ProblemDetail handleLlm(LlmException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, e.getMessage());
    }

    @ExceptionHandler(AgentException.class)
    ProblemDetail handleAgent(AgentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
