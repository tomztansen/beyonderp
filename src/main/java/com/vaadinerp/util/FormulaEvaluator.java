package com.vaadinerp.util;

import java.util.Map;

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

    /**
     * Evaluates a target mapping expression (e.g. "coalesce(lineno,0)+1" or "qty * price" or "ifnull(status, 'Draft')").
     * Supports COALESCE/IFNULL functions, variable substitution from srcRecord and destRow, and arithmetic calculations.
     */
    public static Object evaluateTargetExpression(String expression, Map<String, Object> srcRecord, Map<String, Object> destRow) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }
        String expr = expression.trim();

        // 1. Process and resolve coalesce(...) and ifnull(...) calls from inside out
        while (true) {
            int idxCoalesce = indexOfIgnoreCase(expr, "coalesce(");
            int idxIfNull = indexOfIgnoreCase(expr, "ifnull(");
            int startIdx = -1;
            int funcNameLen = 0;

            if (idxCoalesce != -1 && (idxIfNull == -1 || idxCoalesce < idxIfNull)) {
                startIdx = idxCoalesce;
                funcNameLen = 9; // length of "coalesce("
            } else if (idxIfNull != -1) {
                startIdx = idxIfNull;
                funcNameLen = 7; // length of "ifnull("
            } else {
                break;
            }

            // Find matching closing parenthesis
            int openPos = startIdx + funcNameLen - 1;
            int parenDepth = 1;
            int endPos = -1;
            for (int i = openPos + 1; i < expr.length(); i++) {
                char c = expr.charAt(i);
                if (c == '(') parenDepth++;
                else if (c == ')') {
                    parenDepth--;
                    if (parenDepth == 0) {
                        endPos = i;
                        break;
                    }
                }
            }

            if (endPos == -1) {
                break; // Mismatched parenthesis, abort loop
            }

            String innerArgs = expr.substring(openPos + 1, endPos);
            String[] args = innerArgs.split(",");
            Object resolvedVal = null;
            for (String arg : args) {
                arg = arg.trim();
                if (arg.isEmpty()) continue;

                // Check string literal
                if ((arg.startsWith("'") && arg.endsWith("'")) || (arg.startsWith("\"") && arg.endsWith("\""))) {
                    if (arg.length() >= 2) {
                        resolvedVal = arg.substring(1, arg.length() - 1);
                        break;
                    }
                }
                // Check boolean literal
                if ("true".equalsIgnoreCase(arg) || "false".equalsIgnoreCase(arg)) {
                    resolvedVal = Boolean.parseBoolean(arg);
                    break;
                }
                // Check null literal
                if ("null".equalsIgnoreCase(arg)) {
                    continue;
                }
                // Check numeric literal
                try {
                    if (arg.contains(".")) {
                        resolvedVal = new java.math.BigDecimal(arg);
                        break;
                    } else {
                        resolvedVal = Integer.parseInt(arg);
                        break;
                    }
                } catch (Exception ignored) {
                    try {
                        resolvedVal = Long.parseLong(arg);
                        break;
                    } catch (Exception ignored2) {}
                }

                // Look up as variable name in srcRecord / destRow
                Object lookupVal = getFieldValueCaseInsensitive(arg, srcRecord, destRow);
                if (lookupVal != null) {
                    resolvedVal = lookupVal;
                    break;
                }
            }

            String replacement = resolvedVal != null ? resolvedVal.toString() : "0";
            if (resolvedVal instanceof String) {
                // If the resolved value is a string and the outer expr has no math, keep quotes or replace
                replacement = "'" + resolvedVal.toString() + "'";
            }
            expr = expr.substring(0, startIdx) + replacement + expr.substring(endPos + 1);
        }

        expr = expr.trim();
        // Remove surrounding single/double quotes if it resolved to a pure string literal
        if ((expr.startsWith("'") && expr.endsWith("'")) || (expr.startsWith("\"") && expr.endsWith("\""))) {
            if (expr.length() >= 2) return expr.substring(1, expr.length() - 1);
        }

        // Check if there are arithmetic operators (+, -, *, /) remaining or if it's purely math
        boolean hasMath = expr.contains("+") || expr.contains("*") || expr.contains("/") || (expr.contains("-") && !expr.startsWith("-"));
        if (hasMath) {
            // Build combined variables map for any remaining variables outside coalesce
            java.util.Map<String, Object> combinedVars = new java.util.HashMap<>();
            if (destRow != null) combinedVars.putAll(destRow);
            if (srcRecord != null) combinedVars.putAll(srcRecord);

            double dResult = evaluate(expr, combinedVars);
            if (dResult == Math.floor(dResult) && !Double.isInfinite(dResult) && dResult >= Integer.MIN_VALUE && dResult <= Integer.MAX_VALUE) {
                return (int) dResult;
            } else if (dResult == Math.floor(dResult) && !Double.isInfinite(dResult) && dResult >= Long.MIN_VALUE && dResult <= Long.MAX_VALUE) {
                return (long) dResult;
            }
            return dResult;
        }

        // Check if final expr is a simple number literal or boolean after substitution
        try {
            if (expr.contains(".")) {
                return new java.math.BigDecimal(expr);
            } else {
                return Integer.parseInt(expr);
            }
        } catch (Exception ignored) {
            try {
                return Long.parseLong(expr);
            } catch (Exception ignored2) {}
        }
        if ("true".equalsIgnoreCase(expr) || "false".equalsIgnoreCase(expr)) {
            return Boolean.parseBoolean(expr);
        }
        if ("null".equalsIgnoreCase(expr)) {
            return null;
        }

        // If not math and not literal, try one final variable lookup
        Object finalLookup = getFieldValueCaseInsensitive(expr, srcRecord, destRow);
        return finalLookup != null ? finalLookup : expr;
    }

    private static int indexOfIgnoreCase(String str, String sub) {
        if (str == null || sub == null) return -1;
        return str.toLowerCase().indexOf(sub.toLowerCase());
    }

    private static Object getFieldValueCaseInsensitive(String key, Map<String, Object> srcRecord, Map<String, Object> destRow) {
        if (key == null) return null;
        String cleanKey = key.trim();
        if (cleanKey.toLowerCase().startsWith("source.")) {
            cleanKey = cleanKey.substring(7);
            return findInMap(srcRecord, cleanKey);
        } else if (cleanKey.toLowerCase().startsWith("dest.") || cleanKey.toLowerCase().startsWith("detail.")) {
            cleanKey = cleanKey.substring(cleanKey.indexOf('.') + 1);
            return findInMap(destRow, cleanKey);
        }
        Object val = findInMap(srcRecord, cleanKey);
        if (val == null) {
            val = findInMap(destRow, cleanKey);
        }
        return val;
    }

    private static Object findInMap(Map<String, Object> map, String key) {
        if (map == null || key == null) return null;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        String cleanKey = key.replaceAll("[_\\s-]+", "");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().replaceAll("[_\\s-]+", "").equalsIgnoreCase(cleanKey)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
