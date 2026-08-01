package com.geckolib.loading.math.value;

import java.util.Set;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;
import com.geckolib.loading.math.Operator;

/** A binary calculation. */
public final class Calculation implements MathValue {

    private final Operator operator;
    private final MathValue argA;
    private final MathValue argB;
    private final boolean mutable;
    private final Set<Variable> usedVariables;
    private double cachedValue;
    private boolean cached;

    public Calculation(Operator operator, MathValue argA, MathValue argB) {
        this.operator = operator;
        this.argA = argA;
        this.argB = argB;
        this.mutable = argA.isMutable() || argB.isMutable();
        this.usedVariables = MathValue.collectUsedVariables(argA, argB);
    }

    @Override
    public double get(ControllerState controllerState) {
        if (this.mutable) return compute(controllerState);

        if (!this.cached) {
            this.cachedValue = compute(controllerState);
            this.cached = true;
        }

        return this.cachedValue;
    }

    private double compute(ControllerState controllerState) {
        return this.operator.compute(this.argA.get(controllerState), this.argB.get(controllerState));
    }

    @Override
    public boolean isMutable() {
        return this.mutable;
    }

    @Override
    public Set<Variable> getUsedVariables() {
        return this.usedVariables;
    }

    @Override
    public String toString() {
        return this.argA + " " + this.operator.symbol() + " " + this.argB;
    }
}
