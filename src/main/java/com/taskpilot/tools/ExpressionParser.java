package com.taskpilot.tools;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Safe arithmetic evaluator (no eval/scripting). Supports + - * / % ^,
 * parentheses,
 * unary minus and decimals. Grammar:
 * expr := term (('+' | '-') term)*
 * term := unary (('*' | '/' | '%') unary)*
 * unary := ('-' | '+') unary | power
 * power := primary ('^' unary)?
 * primary:= number | '(' expr ')'
 */
final class ExpressionParser {

    private final String text;
    private int pos;

    private ExpressionParser(String text) {
        this.text = text;
    }

    static String evaluate(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Expression is empty");
        }
        double value = new ExpressionParser(expression).parseAll();
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new ArithmeticException("Result is not a finite number");
        }
        return new BigDecimal(value).round(new MathContext(12)).stripTrailingZeros().toPlainString();
    }

    private double parseAll() {
        double value = expr();
        skipWhitespace();
        if (pos < text.length()) {
            throw new IllegalArgumentException("Unexpected '" + text.charAt(pos) + "' at position " + pos);
        }
        return value;
    }

    private double expr() {
        double value = term();
        while (true) {
            if (consume('+')) {
                value += term();
            } else if (consume('-')) {
                value -= term();
            } else {
                return value;
            }
        }
    }

    private double term() {
        double value = unary();
        while (true) {
            if (consume('*')) {
                value *= unary();
            } else if (consume('/')) {
                double divisor = unary();
                if (divisor == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                value /= divisor;
            } else if (consume('%')) {
                double divisor = unary();
                if (divisor == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                value %= divisor;
            } else {
                return value;
            }
        }
    }

    private double unary() {
        if (consume('-')) {
            return -unary();
        }
        if (consume('+')) {
            return unary();
        }
        return power();
    }

    private double power() {
        double base = primary();
        if (consume('^')) {
            return Math.pow(base, unary());
        }
        return base;
    }

    private double primary() {
        skipWhitespace();
        if (consume('(')) {
            double value = expr();
            if (!consume(')')) {
                throw new IllegalArgumentException("Missing closing parenthesis");
            }
            return value;
        }
        int start = pos;
        while (pos < text.length() && (Character.isDigit(text.charAt(pos)) || text.charAt(pos) == '.')) {
            pos++;
        }
        if (start == pos) {
            throw new IllegalArgumentException(pos < text.length()
                    ? "Unexpected '" + text.charAt(pos) + "' at position " + pos
                    : "Unexpected end of expression");
        }
        try {
            return Double.parseDouble(text.substring(start, pos));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + text.substring(start, pos));
        }
    }

    private boolean consume(char expected) {
        skipWhitespace();
        if (pos < text.length() && text.charAt(pos) == expected) {
            pos++;
            return true;
        }
        return false;
    }

    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
    }
}
