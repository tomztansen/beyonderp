package com.vaadinerp.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormulaEvaluator {

    /**
     * Evaluates a mathematical expression (e.g. "qty * price") by replacing variables
     * with values from the given map and calculating the result.
     * Supports +, -, *, /, parentheses, and decimals.
     */
    public static double evaluate(String expression, Map<String, Object> variables) {
        if (expression == null || expression.trim().isEmpty()) {
            return 0.0;
        }

        String parsedExpr = expression;
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                String varName = entry.getKey();
                Object val = entry.getValue();
                double dVal = 0.0;
                if (val instanceof Number) {
                    dVal = ((Number) val).doubleValue();
                } else if (val != null) {
                    try {
                        dVal = Double.parseDouble(val.toString());
                    } catch (NumberFormatException ignored) {}
                }
                // Use regex with word boundaries to replace only the exact field name
                parsedExpr = parsedExpr.replaceAll("\\b" + varName + "\\b", String.valueOf(dVal));
            }
        }

        // Evaluate the cleaned expression using a simple recursive-descent parser
        try {
            final String finalExpr = parsedExpr;
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < finalExpr.length()) ? finalExpr.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                double parse() {
                    nextChar();
                    double x = parseExpression();
                    if (pos < finalExpr.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                    return x;
                }

                double parseExpression() {
                    double x = parseTerm();
                    for (;;) {
                        if      (eat('+')) x += parseTerm(); // addition
                        else if (eat('-')) x -= parseTerm(); // subtraction
                        else return x;
                    }
                }

                double parseTerm() {
                    double x = parseFactor();
                    for (;;) {
                        if      (eat('*')) x *= parseFactor(); // multiplication
                        else if (eat('/')) {
                            double divisor = parseFactor();
                            x = divisor == 0 ? 0.0 : x / divisor; // protect against division by zero
                        }
                        else return x;
                    }
                }

                double parseFactor() {
                    if (eat('+')) return parseFactor(); // unary plus
                    if (eat('-')) return -parseFactor(); // unary minus

                    double x;
                    int startPos = this.pos;
                    if (eat('(')) { // parentheses
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = Double.parseDouble(finalExpr.substring(startPos, this.pos));
                    } else {
                        // Skip any alphanumeric words (possibly variables that weren't replaced, default to 0)
                        if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
                            while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_') {
                                nextChar();
                            }
                        } else {
                            nextChar();
                        }
                        x = 0.0;
                    }

                    return x;
                }
            }.parse();
        } catch (Exception e) {
            return 0.0;
        }
    }
}
