package com.geckolib.loading.math.function;

import java.util.Set;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;
import com.geckolib.loading.math.value.Variable;

/** Base type for registered Molang functions. */
public abstract class MathFunction implements MathValue {

    private final boolean mutable;
    private final Set<Variable> usedVariables;
    private double cachedValue;
    private boolean cached;

    protected MathFunction(MathValue... values) {
        this(false, values);
    }

    protected MathFunction(boolean alwaysMutable, MathValue... values) {
        validate(values);
        this.mutable = alwaysMutable || containsMutableValue(values);
        this.usedVariables = MathValue.collectUsedVariables(values);
    }

    public abstract String getName();

    public abstract double compute(ControllerState controllerState);

    public abstract int getMinArgs();

    public abstract MathValue[] getArgs();

    @Override
    public final double get(ControllerState controllerState) {
        if (this.mutable) return compute(controllerState);

        if (!this.cached) {
            this.cachedValue = compute(controllerState);
            this.cached = true;
        }

        return this.cachedValue;
    }

    public boolean isMutable(MathValue... values) {
        return containsMutableValue(values);
    }

    private static boolean containsMutableValue(MathValue... values) {
        for (MathValue value : values) {
            if (value.isMutable()) return true;
        }

        return false;
    }

    public void validate(MathValue... inputs) {
        if (inputs.length < getMinArgs()) throw new IllegalArgumentException(
            "Function '" + getName() + "' requires at least " + getMinArgs() + " arguments; got " + inputs.length);
    }

    @Override
    public final boolean isMutable() {
        return this.mutable;
    }

    @Override
    public Set<Variable> getUsedVariables() {
        return this.usedVariables;
    }

    @FunctionalInterface
    public interface Factory<T extends MathFunction> {

        T create(MathValue... values);
    }
}
