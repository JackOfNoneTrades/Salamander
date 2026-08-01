package com.geckolib.loading.math.value;

import java.util.Set;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;

/** Molang boolean negation, where zero is false and non-zero is true. */
public final class BooleanNegate implements MathValue {

    private final MathValue value;

    public BooleanNegate(MathValue value) {
        this.value = value;
    }

    @Override
    public double get(ControllerState controllerState) {
        return this.value.get(controllerState) == 0 ? 1 : 0;
    }

    @Override
    public boolean isMutable() {
        return this.value.isMutable();
    }

    @Override
    public Set<Variable> getUsedVariables() {
        return this.value.getUsedVariables();
    }
}
