package com.geckolib.loading.math;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** A binary Molang operator. */
public final class Operator implements Comparable<Operator> {

    private static final Map<String, Operator> OPERATORS = new LinkedHashMap<>();
    private static int longestOperator;

    public static final Operator ADD = register("+", 4, (a, b) -> a + b);
    public static final Operator SUB = register("-", 4, (a, b) -> a - b);
    public static final Operator MUL = register("*", 5, (a, b) -> a * b);
    public static final Operator DIV = register("/", 5, (a, b) -> b == 0 ? a : a / b);
    public static final Operator MOD = register("%", 5, (a, b) -> b == 0 ? a : a % b);
    public static final Operator POW = register("^", 6, Math::pow);
    public static final Operator LT = register("<", 3, (a, b) -> a < b ? 1 : 0);
    public static final Operator LTE = register("<=", 3, (a, b) -> a <= b ? 1 : 0);
    public static final Operator GT = register(">", 3, (a, b) -> a > b ? 1 : 0);
    public static final Operator GTE = register(">=", 3, (a, b) -> a >= b ? 1 : 0);
    public static final Operator EQUAL = register("==", 3, (a, b) -> Math.abs(a - b) < 0.00001 ? 1 : 0);
    public static final Operator NOT_EQUAL = register("!=", 3, (a, b) -> Math.abs(a - b) >= 0.00001 ? 1 : 0);
    public static final Operator AND = register("&&", 2, (a, b) -> a != 0 && b != 0 ? 1 : 0);
    public static final Operator OR = register("||", 1, (a, b) -> a != 0 || b != 0 ? 1 : 0);

    private final String symbol;
    private final int precedence;
    private final Operation operation;

    private Operator(String symbol, int precedence, Operation operation) {
        this.symbol = symbol;
        this.precedence = precedence;
        this.operation = operation;
    }

    public static Operator register(String symbol, int precedence, Operation operation) {
        if (OPERATORS.containsKey(symbol))
            throw new IllegalArgumentException("Operator is already registered: " + symbol);

        Operator operator = new Operator(symbol, precedence, operation);

        OPERATORS.put(symbol, operator);
        longestOperator = Math.max(longestOperator, symbol.length());

        return operator;
    }

    public static boolean isOperator(String symbol) {
        return OPERATORS.containsKey(symbol);
    }

    public static Optional<Operator> getOperatorFor(String symbol) {
        return Optional.ofNullable(OPERATORS.get(symbol));
    }

    public static int maxOperatorLength() {
        return longestOperator;
    }

    public static Map<String, Operator> registeredOperators() {
        return Collections.unmodifiableMap(OPERATORS);
    }

    public String symbol() {
        return this.symbol;
    }

    public int precedence() {
        return this.precedence;
    }

    public double compute(double argA, double argB) {
        return this.operation.compute(argA, argB);
    }

    @Override
    public int compareTo(Operator other) {
        return Integer.compare(this.precedence, other.precedence);
    }

    @FunctionalInterface
    public interface Operation {

        double compute(double argA, double argB);
    }
}
