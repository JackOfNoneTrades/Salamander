package com.geckolib.loading.math.value;

import java.util.Set;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;

/** Arithmetic negation. */
public final class Negative implements MathValue {

    private final MathValue value;

    public Negative(MathValue value) {
        this.value = value;
    }

    @Override
    public double get(ControllerState controllerState) {
        return -this.value.get(controllerState);
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
