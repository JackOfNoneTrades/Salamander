package com.geckolib.loading.math.value;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;

/** An immutable numeric value. */
public final class Constant implements MathValue {

    private final double value;

    public Constant(double value) {
        this.value = value;
    }

    public double value() {
        return this.value;
    }

    @Override
    public double get(ControllerState controllerState) {
        return this.value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}
