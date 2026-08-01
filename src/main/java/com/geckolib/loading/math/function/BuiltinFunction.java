package com.geckolib.loading.math.function;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;

/** Internal function implementation used by the standard Molang function table. */
public final class BuiltinFunction extends MathFunction {

    private final String name;
    private final int minimumArguments;
    private final int maximumArguments;
    private final MathValue[] arguments;
    private final Computer computer;

    public BuiltinFunction(String name, int minimumArguments, int maximumArguments, Computer computer,
        boolean alwaysMutable, MathValue... arguments) {
        super(alwaysMutable, validateArgumentCount(name, minimumArguments, maximumArguments, arguments));
        this.name = name;
        this.minimumArguments = minimumArguments;
        this.maximumArguments = maximumArguments;
        this.arguments = arguments.clone();
        this.computer = computer;
    }

    private static MathValue[] validateArgumentCount(String name, int minimumArguments, int maximumArguments,
        MathValue[] arguments) {
        if (arguments.length < minimumArguments || arguments.length > maximumArguments) {
            throw new IllegalArgumentException(
                "Function '" + name
                    + "' expects "
                    + minimumArguments
                    + ".."
                    + maximumArguments
                    + " arguments; got "
                    + arguments.length);
        }

        return arguments;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public double compute(ControllerState controllerState) {
        double[] values = new double[this.arguments.length];

        for (int i = 0; i < values.length; i++) {
            values[i] = this.arguments[i].get(controllerState);
        }

        return this.computer.compute(values);
    }

    @Override
    public int getMinArgs() {
        return this.minimumArguments;
    }

    public int getMaxArgs() {
        return this.maximumArguments;
    }

    @Override
    public MathValue[] getArgs() {
        return this.arguments.clone();
    }

    @FunctionalInterface
    public interface Computer {

        double compute(double[] arguments);
    }
}
