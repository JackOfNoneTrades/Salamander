package com.geckolib.loading.math;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.definition.animation.DoubleOrString;
import com.geckolib.loading.math.function.BuiltinFunction;
import com.geckolib.loading.math.function.MathFunction;
import com.geckolib.loading.math.value.BooleanNegate;
import com.geckolib.loading.math.value.Calculation;
import com.geckolib.loading.math.value.CompoundValue;
import com.geckolib.loading.math.value.Constant;
import com.geckolib.loading.math.value.Negative;
import com.geckolib.loading.math.value.Ternary;
import com.geckolib.loading.math.value.Variable;
import com.geckolib.loading.math.value.VariableAssignment;

/**
 * Compiles the expression subset used by GeckoLib 5 animation JSON files.
 *
 * <p>
 * The parser follows Molang numeric boolean semantics and supports compound statements, local assignments,
 * conditionals, standard operators, query aliases, and GeckoLib's standard math functions.
 */
public class MathParser {

    private static final Map<String, MathFunction.Factory<?>> FUNCTION_FACTORIES = new ConcurrentHashMap<>();
    private static final Map<String, Variable> VARIABLES = new ConcurrentHashMap<>();
    private static final double DEG_TO_RAD = Math.PI / 180d;
    private static final double RAD_TO_DEG = 180d / Math.PI;

    static {
        registerBuiltins();
    }

    protected final Deduplicator deduplicator;

    protected MathParser() {
        this(Deduplicator.NONE);
    }

    protected MathParser(Deduplicator deduplicator) {
        this.deduplicator = deduplicator;
    }

    public static MathParser create() {
        return new MathParser();
    }

    public static MathParser createWithDeduplication() {
        return new MathParser(Deduplicator.defaultImpl());
    }

    public static void registerFunction(String name, MathFunction.Factory<?> factory) {
        FUNCTION_FACTORIES.put(normalize(name), factory);
    }

    public static void registerVariable(Variable variable) {
        VARIABLES.put(Variable.normalizeName(variable.name()), variable);
    }

    public static void setVariable(String name, ToDoubleFunction<ControllerState> value) {
        getVariableFor(name).set(value);
    }

    public static Variable getVariableFor(String name) {
        String normalizedName = Variable.normalizeName(name);

        return VARIABLES.computeIfAbsent(normalizedName, Variable::new);
    }

    public static boolean isFunctionRegistered(String name) {
        return FUNCTION_FACTORIES.containsKey(normalize(name));
    }

    public MathValue compileDoubleOrString(DoubleOrString value) {
        return value.isDouble() ? compileConstant(value.doubleValue()) : compileMolang(value.stringValue());
    }

    public MathValue compileConstant(double value) {
        return this.deduplicator.apply(value);
    }

    public MathValue compileMolang(String expression) {
        if (expression == null) throw new IllegalArgumentException("Molang expression cannot be null");

        final String normalizedExpression = expression.trim();

        return this.deduplicator.apply(normalizedExpression, this::compileExpression);
    }

    @SafeVarargs
    public final MathValue wrap(MathValue value, UnaryOperator<MathValue>... wrappers) {
        MathValue wrapped = value;

        for (UnaryOperator<MathValue> wrapper : wrappers) {
            wrapped = wrapper.apply(wrapped);
        }

        return deduplicate(wrapped);
    }

    protected MathValue deduplicate(MathValue value) {
        return this.deduplicator.apply(value);
    }

    protected Optional<MathValue> buildFunction(String name, MathValue... values) {
        MathFunction.Factory<?> factory = FUNCTION_FACTORIES.get(normalize(name));

        return factory == null ? Optional.empty() : Optional.of(deduplicate(factory.create(values)));
    }

    protected MathValue compileExpression(String expression) {
        Parser parser = new Parser(expression);
        List<MathValue> statements = new ArrayList<>();

        while (!parser.isAtEnd()) {
            boolean returning = parser.matchKeyword("return");

            statements.add(parser.parseAssignment());

            if (returning) {
                parser.consumeOptionalSemicolon();
                parser.ignoreRemainingInput();
                break;
            }

            if (!parser.consumeOptionalSemicolon() && !parser.isAtEnd())
                throw parser.error("Expected ';' between Molang statements");
        }

        if (statements.isEmpty()) return compileConstant(0);

        if (statements.size() == 1) return deduplicate(statements.get(0));

        return deduplicate(new CompoundValue(statements.toArray(new MathValue[0])));
    }

    private static void registerBuiltins() {
        register("math.abs", 1, 1, false, args -> Math.abs(args[0]));
        register("math.acos", 1, 1, false, args -> Math.acos(args[0] * DEG_TO_RAD));
        register("math.asin", 1, 1, false, args -> Math.asin(args[0] * DEG_TO_RAD));
        register("math.atan", 1, 1, false, args -> Math.atan(args[0] * DEG_TO_RAD));
        register("math.atan2", 2, 2, false, args -> Math.atan2(args[0], args[1]) * RAD_TO_DEG);
        register("math.ceil", 1, 1, false, args -> Math.ceil(args[0]));
        register("math.clamp", 3, 3, false, args -> Math.max(args[1], Math.min(args[2], args[0])));
        register("math.cos", 1, 1, false, args -> Math.cos(args[0] * DEG_TO_RAD));
        register("math.exp", 1, 1, false, args -> Math.exp(args[0]));
        register("math.floor", 1, 1, false, args -> Math.floor(args[0]));
        register("math.hermite_blend", 1, 1, false, args -> 3 * args[0] * args[0] - 2 * args[0] * args[0] * args[0]);
        register("math.lerp", 3, 3, false, args -> args[0] + (args[1] - args[0]) * args[2]);
        register("math.lerprotate", 3, 3, false, args -> lerpDegrees(args[2], args[0], args[1]));
        register("math.ln", 1, 1, false, args -> Math.log(args[0]));
        register("math.max", 2, 2, false, args -> Math.max(args[0], args[1]));
        register("math.min", 2, 2, false, args -> Math.min(args[0], args[1]));
        register("math.mod", 2, 2, false, args -> args[1] == 0 ? args[0] : args[0] % args[1]);
        register("math.pi", 0, 0, false, args -> Math.PI);
        register("math.pow", 2, 2, false, args -> Math.pow(args[0], args[1]));
        register("math.round", 1, 1, false, args -> Math.round(args[0]));
        register("math.sin", 1, 1, false, args -> Math.sin(args[0] * DEG_TO_RAD));
        register("math.sqrt", 1, 1, false, args -> Math.sqrt(args[0]));
        register("math.to_deg", 1, 1, false, args -> args[0] * RAD_TO_DEG);
        register("math.to_rad", 1, 1, false, args -> args[0] * DEG_TO_RAD);
        register("math.trunc", 1, 1, false, args -> args[0] < 0 ? Math.ceil(args[0]) : Math.floor(args[0]));
        register("math.random", 1, 3, true, MathParser::random);
        register("math.random_integer", 1, 3, true, MathParser::randomInteger);
        register("math.die_roll", 3, 4, true, args -> dieRoll(args, false));
        register("math.die_roll_integer", 3, 4, true, args -> dieRoll(args, true));
    }

    private static void register(String name, int minArgs, int maxArgs, boolean mutable,
        BuiltinFunction.Computer computer) {
        registerFunction(
            name,
            args -> new BuiltinFunction(name, minArgs, maxArgs, computer, mutable && args.length < maxArgs, args));
    }

    private static double random(double[] args) {
        Random random = args.length >= 3 ? new Random((long) args[2]) : new Random();
        double value = random.nextDouble();

        if (args.length == 1) return value * args[0];

        double min = Math.min(args[0], args[1]);
        double max = Math.max(args[0], args[1]);

        return min + value * (max - min);
    }

    private static double randomInteger(double[] args) {
        Random random = args.length >= 3 ? new Random((long) args[2]) : new Random();
        int first = (int) Math.round(args[0]);

        if (args.length == 1) return random.nextInt(first + 1);

        int second = (int) Math.round(args[1]);
        int min = Math.min(first, second);
        int max = Math.max(first, second);

        return min + random.nextInt(max + 1 - min);
    }

    private static double dieRoll(double[] args, boolean integer) {
        int rolls = (int) Math.floor(args[0]);
        double min = Math.min(args[1], args[2]);
        double max = Math.max(args[1], args[2]);
        Random random = args.length >= 4 ? new Random((long) args[3]) : new Random();
        double result = 0;

        for (int i = 0; i < rolls; i++) {
            if (integer) {
                int integerMin = (int) Math.floor(min);
                int integerMax = (int) Math.ceil(max);

                result += integerMin + random.nextInt(integerMax + 1 - integerMin);
            } else {
                result += min + random.nextDouble() * (max - min);
            }
        }

        return result;
    }

    private static double lerpDegrees(double delta, double start, double end) {
        double difference = wrapDegrees(end - start);

        return start + delta * difference;
    }

    private static double wrapDegrees(double degrees) {
        double wrapped = degrees % 360;

        if (wrapped >= 180) wrapped -= 360;

        if (wrapped < -180) wrapped += 360;

        return wrapped;
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private final class Parser {

        private final List<Token> tokens;
        private int current;

        private Parser(String expression) {
            this.tokens = tokenize(expression);
        }

        private MathValue parseAssignment() {
            if (check(TokenType.IDENTIFIER) && checkNext(TokenType.ASSIGN)) {
                Variable variable = getVariableFor(advance().text);

                advance();

                return deduplicate(new VariableAssignment(variable, parseAssignment()));
            }

            return parseTernary();
        }

        private MathValue parseTernary() {
            MathValue condition = parseBinary(1);

            if (!match(TokenType.QUESTION)) return condition;

            MathValue trueValue = parseAssignment();

            consume(TokenType.COLON, "Expected ':' in ternary expression");

            return deduplicate(new Ternary(condition, trueValue, parseAssignment()));
        }

        private MathValue parseBinary(int minimumPrecedence) {
            MathValue left = parseUnary();

            while (check(TokenType.OPERATOR)) {
                Operator operator = Operator.getOperatorFor(peek().text)
                    .orElse(null);

                if (operator == null || operator.precedence() < minimumPrecedence) break;

                advance();

                int nextMinimum = operator == Operator.POW ? operator.precedence() : operator.precedence() + 1;
                MathValue right = parseBinary(nextMinimum);

                left = deduplicate(new Calculation(operator, left, right));
            }

            return left;
        }

        private MathValue parseUnary() {
            if (matchOperator("-")) return deduplicate(new Negative(parseUnary()));

            if (matchOperator("+")) return parseUnary();

            if (matchOperator("!")) return deduplicate(new BooleanNegate(parseUnary()));

            return parsePrimary();
        }

        private MathValue parsePrimary() {
            if (match(TokenType.NUMBER)) return compileConstant(Double.parseDouble(previous().text));

            if (match(TokenType.LEFT_PAREN)) {
                MathValue grouped = parseAssignment();

                consume(TokenType.RIGHT_PAREN, "Expected ')' after expression");

                return grouped;
            }

            if (match(TokenType.IDENTIFIER)) {
                String identifier = previous().text;

                if ("true".equals(identifier)) return compileConstant(1);

                if ("false".equals(identifier)) return compileConstant(0);

                if (match(TokenType.LEFT_PAREN)) {
                    List<MathValue> arguments = new ArrayList<>();

                    if (!check(TokenType.RIGHT_PAREN)) {
                        do {
                            arguments.add(parseAssignment());
                        } while (match(TokenType.COMMA));
                    }

                    consume(TokenType.RIGHT_PAREN, "Expected ')' after function arguments");

                    return buildFunction(identifier, arguments.toArray(new MathValue[0]))
                        .orElseThrow(() -> error("Unknown Molang function '" + identifier + "'"));
                }

                if (isFunctionRegistered(identifier)) {
                    return buildFunction(identifier)
                        .orElseThrow(() -> error("Unknown Molang function '" + identifier + "'"));
                }

                return getVariableFor(identifier);
            }

            throw error("Expected a Molang value");
        }

        private boolean matchKeyword(String keyword) {
            if (!check(TokenType.IDENTIFIER) || !keyword.equals(peek().text)) return false;

            advance();

            return true;
        }

        private boolean matchOperator(String symbol) {
            if (!check(TokenType.OPERATOR) || !symbol.equals(peek().text)) return false;

            advance();

            return true;
        }

        private boolean match(TokenType type) {
            if (!check(type)) return false;

            advance();

            return true;
        }

        private Token consume(TokenType type, String message) {
            if (check(type)) return advance();

            throw error(message);
        }

        private boolean consumeOptionalSemicolon() {
            return match(TokenType.SEMICOLON);
        }

        private boolean check(TokenType type) {
            return peek().type == type;
        }

        private boolean checkNext(TokenType type) {
            return this.current + 1 < this.tokens.size() && this.tokens.get(this.current + 1).type == type;
        }

        private Token advance() {
            if (!isAtEnd()) this.current++;

            return previous();
        }

        private boolean isAtEnd() {
            return peek().type == TokenType.END;
        }

        private Token peek() {
            return this.tokens.get(this.current);
        }

        private Token previous() {
            return this.tokens.get(this.current - 1);
        }

        private void ignoreRemainingInput() {
            this.current = this.tokens.size() - 1;
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at character " + peek().offset);
        }
    }

    private static List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<>();
        int index = 0;

        while (index < expression.length()) {
            char character = expression.charAt(index);

            if (Character.isWhitespace(character)) {
                index++;
                continue;
            }

            if (Character.isDigit(character) || character == '.' && index + 1 < expression.length()
                && Character.isDigit(expression.charAt(index + 1))) {
                int start = index++;

                while (index < expression.length() && Character.isDigit(expression.charAt(index))) index++;

                if (index < expression.length() && expression.charAt(index) == '.') {
                    index++;

                    while (index < expression.length() && Character.isDigit(expression.charAt(index))) index++;
                }

                tokens.add(new Token(TokenType.NUMBER, expression.substring(start, index), start));
                continue;
            }

            if (Character.isLetter(character) || character == '_') {
                int start = index++;

                while (index < expression.length()) {
                    char next = expression.charAt(index);

                    if (!Character.isLetterOrDigit(next) && next != '_' && next != '.') break;

                    index++;
                }

                tokens.add(new Token(TokenType.IDENTIFIER, normalize(expression.substring(start, index)), start));
                continue;
            }

            switch (character) {
                case '(':
                    tokens.add(new Token(TokenType.LEFT_PAREN, "(", index++));
                    continue;
                case ')':
                    tokens.add(new Token(TokenType.RIGHT_PAREN, ")", index++));
                    continue;
                case ',':
                    tokens.add(new Token(TokenType.COMMA, ",", index++));
                    continue;
                case '?':
                    tokens.add(new Token(TokenType.QUESTION, "?", index++));
                    continue;
                case ':':
                    tokens.add(new Token(TokenType.COLON, ":", index++));
                    continue;
                case ';':
                    tokens.add(new Token(TokenType.SEMICOLON, ";", index++));
                    continue;
                default:
                    break;
            }

            String twoCharacters = index + 1 < expression.length() ? expression.substring(index, index + 2) : "";

            if (Operator.isOperator(twoCharacters)) {
                tokens.add(new Token(TokenType.OPERATOR, twoCharacters, index));
                index += 2;
                continue;
            }

            String symbol = String.valueOf(character);

            if ("=".equals(symbol)) {
                tokens.add(new Token(TokenType.ASSIGN, symbol, index++));
                continue;
            }

            if (Operator.isOperator(symbol) || "!".equals(symbol)) {
                tokens.add(new Token(TokenType.OPERATOR, symbol, index++));
                continue;
            }

            throw new IllegalArgumentException("Invalid Molang character '" + character + "' at character " + index);
        }

        tokens.add(new Token(TokenType.END, "", expression.length()));

        return tokens;
    }

    private enum TokenType {
        NUMBER,
        IDENTIFIER,
        OPERATOR,
        ASSIGN,
        LEFT_PAREN,
        RIGHT_PAREN,
        COMMA,
        QUESTION,
        COLON,
        SEMICOLON,
        END
    }

    private static final class Token {

        private final TokenType type;
        private final String text;
        private final int offset;

        private Token(TokenType type, String text, int offset) {
            this.type = type;
            this.text = text;
            this.offset = offset;
        }
    }

    public interface Deduplicator {

        Deduplicator NONE = new Deduplicator() {

            @Override
            public MathValue apply(MathValue value) {
                return value;
            }

            @Override
            public MathValue apply(double constant) {
                return new Constant(constant);
            }

            @Override
            public MathValue apply(String expression, Function<String, MathValue> parser) {
                return parser.apply(expression);
            }
        };

        MathValue apply(MathValue value);

        MathValue apply(double constant);

        MathValue apply(String expression, Function<String, MathValue> parser);

        static Deduplicator defaultImpl() {
            return new Deduplicator() {

                private final ConcurrentHashMap<Double, MathValue> values = new ConcurrentHashMap<>();
                private final ConcurrentHashMap<String, MathValue> expressions = new ConcurrentHashMap<>();

                @Override
                public MathValue apply(MathValue value) {
                    if (value.isMutable()) return value;

                    return apply(value.get(null));
                }

                @Override
                public MathValue apply(double constant) {
                    return this.values.computeIfAbsent(constant, Constant::new);
                }

                @Override
                public MathValue apply(String expression, Function<String, MathValue> parser) {
                    return this.expressions.computeIfAbsent(expression, parser);
                }
            };
        }
    }
}
