package com.geckolib.loading.math.value;

import java.util.Set;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;

/** A sequence of Molang statements whose final statement supplies the result. */
public final class CompoundValue implements MathValue {

    private final MathValue[] subValues;
    private final Set<Variable> usedVariables;

    public CompoundValue(MathValue[] subValues) {
        if (subValues.length == 0)
            throw new IllegalArgumentException("A compound value must contain at least one expression");

        this.subValues = subValues.clone();
        this.usedVariables = MathValue.collectUsedVariables(subValues);
    }

    @Override
    public double get(ControllerState controllerState) {
        double result = 0;

        for (MathValue subValue : this.subValues) {
            result = subValue.get(controllerState);
        }

        return result;
    }

    @Override
    public boolean isMutable() {
        for (MathValue subValue : this.subValues) {
            if (subValue.isMutable()) return true;
        }

        return false;
    }

    @Override
    public Set<Variable> getUsedVariables() {
        return this.usedVariables;
    }
}
