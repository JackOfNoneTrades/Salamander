package com.geckolib.loading.math.value;

import java.util.Set;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;

/** Molang conditional expression. */
public final class Ternary implements MathValue {

    private final MathValue condition;
    private final MathValue trueValue;
    private final MathValue falseValue;
    private final Set<Variable> usedVariables;

    public Ternary(MathValue condition, MathValue trueValue, MathValue falseValue) {
        this.condition = condition;
        this.trueValue = trueValue;
        this.falseValue = falseValue;
        this.usedVariables = MathValue.collectUsedVariables(condition, trueValue, falseValue);
    }

    @Override
    public double get(ControllerState controllerState) {
        return this.condition.get(controllerState) != 0 ? this.trueValue.get(controllerState)
            : this.falseValue.get(controllerState);
    }

    @Override
    public boolean isMutable() {
        return this.condition.isMutable() || this.trueValue.isMutable() || this.falseValue.isMutable();
    }

    @Override
    public Set<Variable> getUsedVariables() {
        return this.usedVariables;
    }
}
