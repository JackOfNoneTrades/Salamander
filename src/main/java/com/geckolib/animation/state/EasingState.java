package com.geckolib.animation.state;

import com.geckolib.animation.object.EasingType;
import com.geckolib.loading.math.MathValue;

/** Values involved in one eased interpolation. */
public final class EasingState {

    private final EasingType easingType;
    private final MathValue[] easingArgs;
    private final double delta;
    private final double fromValue;
    private final double toValue;

    public EasingState(EasingType easingType, MathValue[] easingArgs, double delta, double fromValue, double toValue) {
        this.easingType = easingType;
        this.easingArgs = easingArgs;
        this.delta = delta;
        this.fromValue = fromValue;
        this.toValue = toValue;
    }

    public EasingType easingType() {
        return this.easingType;
    }

    public MathValue[] easingArgs() {
        return this.easingArgs;
    }

    public double delta() {
        return this.delta;
    }

    public double fromValue() {
        return this.fromValue;
    }

    public double toValue() {
        return this.toValue;
    }

    public Double getFirstEasingArg(ControllerState controllerState) {
        return this.easingArgs.length == 0 ? null : this.easingArgs[0].get(controllerState);
    }

    public double interpolate(ControllerState controllerState) {
        return this.easingType.apply(this, controllerState);
    }
}
