package com.geckolib.loading.math.value;

import java.util.Set;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;

/** A value multiplied by a constant scale. */
public final class ScaledValue implements MathValue {

    private final MathValue value;
    private final double multiplier;

    public ScaledValue(MathValue value, double multiplier) {
        this.value = value;
        this.multiplier = multiplier;
    }

    @Override
    public double get(ControllerState controllerState) {
        return this.value.get(controllerState) * this.multiplier;
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
