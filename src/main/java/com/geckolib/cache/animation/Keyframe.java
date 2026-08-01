package com.geckolib.cache.animation;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import com.geckolib.animation.object.EasingType;
import com.geckolib.loading.math.MathValue;
import com.geckolib.loading.math.value.Variable;

/** Baked animation keyframe data for one axis. */
public final class Keyframe {

    private final double startTime;
    private final double length;
    private final MathValue startValue;
    private final MathValue endValue;
    private final EasingType easingType;
    private final MathValue[] easingArgs;

    public Keyframe(double startTime, double length, MathValue startValue, MathValue endValue) {
        this(startTime, length, startValue, endValue, EasingType.LINEAR);
    }

    public Keyframe(double startTime, double length, MathValue startValue, MathValue endValue, EasingType easingType) {
        this(startTime, length, startValue, endValue, easingType, new MathValue[0]);
    }

    public Keyframe(double startTime, double length, MathValue startValue, MathValue endValue, EasingType easingType,
        MathValue[] easingArgs) {
        this.startTime = startTime;
        this.length = length;
        this.startValue = Objects.requireNonNull(startValue, "startValue");
        this.endValue = Objects.requireNonNull(endValue, "endValue");
        this.easingType = Objects.requireNonNull(easingType, "easingType");
        this.easingArgs = easingArgs.clone();
    }

    public double startTime() {
        return this.startTime;
    }

    public double length() {
        return this.length;
    }

    public MathValue startValue() {
        return this.startValue;
    }

    public MathValue endValue() {
        return this.endValue;
    }

    public EasingType easingType() {
        return this.easingType;
    }

    public MathValue[] easingArgs() {
        return this.easingArgs;
    }

    public Set<Variable> getUsedVariables() {
        MathValue[] values = new MathValue[this.easingArgs.length + 2];

        values[0] = this.startValue;
        values[1] = this.endValue;
        System.arraycopy(this.easingArgs, 0, values, 2, this.easingArgs.length);

        return MathValue.collectUsedVariables(values);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Keyframe)) return false;

        Keyframe other = (Keyframe) obj;

        return Math.abs(this.length - other.length) < 1.0E-5 && this.startValue.equals(other.startValue)
            && this.endValue.equals(other.endValue)
            && this.easingType == other.easingType
            && Arrays.equals(this.easingArgs, other.easingArgs);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(this.length, this.startValue, this.endValue, this.easingType, Arrays.hashCode(this.easingArgs));
    }
}
