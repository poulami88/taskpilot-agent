package com.taskpilot.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class CalculatorToolTest {

    private final CalculatorTool tool = new CalculatorTool();
    private final ObjectMapper mapper = new ObjectMapper();

    private String calc(String expression) throws Exception {
        return tool.execute(mapper.createObjectNode().put("expression", expression));
    }

    @Test
    void respectsOperatorPrecedence() throws Exception {
        assertEquals("14", calc("2+3*4"));
        assertEquals("20", calc("(2+3)*4"));
    }

    @Test
    void handlesPowerAndUnaryMinus() throws Exception {
        assertEquals("-4", calc("-2^2"));
        assertEquals("512", calc("2^3^2"));
        assertEquals("0.5", calc("2^-1"));
    }

    @Test
    void handlesDecimalsAndWhitespace() throws Exception {
        assertEquals("2.5", calc(" 10 / 4 "));
        assertEquals("0.3", calc("0.1 + 0.2"));
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(ArithmeticException.class, () -> calc("1/0"));
        assertThrows(IllegalArgumentException.class, () -> calc("2+"));
        assertThrows(IllegalArgumentException.class, () -> calc("abc"));
        assertThrows(IllegalArgumentException.class, () -> calc("(1+2"));
        assertThrows(IllegalArgumentException.class, () -> calc(""));
    }
}
