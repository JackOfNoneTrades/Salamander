package com.geckolib.animation.object;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleUnaryOperator;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.animation.state.EasingState;
import com.geckolib.cache.animation.Keyframe;
import com.geckolib.loading.math.MathParser;
import com.geckolib.loading.math.MathValue;

/** GeckoLib easing and interpolation function. */
@FunctionalInterface
public interface EasingType {

    Map<String, EasingType> EASING_TYPES = new ConcurrentHashMap<>();

    EasingType LINEAR = register("linear", registerSimple("none", EasingType::linear));
    EasingType STEP = register("step", EasingType::step);
    EasingType EASE_IN_SINE = registerSimple("easeinsine", EasingType::sine);
    EasingType EASE_OUT_SINE = registerSimple("easeoutsine", easeOut(EasingType::sine));
    EasingType EASE_IN_OUT_SINE = registerSimple("easeinoutsine", easeInOut(EasingType::sine));
    EasingType EASE_IN_QUAD = registerSimple("easeinquad", EasingType::quadratic);
    EasingType EASE_OUT_QUAD = registerSimple("easeoutquad", easeOut(EasingType::quadratic));
    EasingType EASE_IN_OUT_QUAD = registerSimple("easeinoutquad", easeInOut(EasingType::quadratic));
    EasingType EASE_IN_CUBIC = registerSimple("easeincubic", EasingType::cubic);
    EasingType EASE_OUT_CUBIC = registerSimple("easeoutcubic", easeOut(EasingType::cubic));
    EasingType EASE_IN_OUT_CUBIC = registerSimple("easeinoutcubic", easeInOut(EasingType::cubic));
    EasingType EASE_IN_QUART = registerSimple("easeinquart", pow(4));
    EasingType EASE_OUT_QUART = registerSimple("easeoutquart", easeOut(pow(4)));
    EasingType EASE_IN_OUT_QUART = registerSimple("easeinoutquart", easeInOut(pow(4)));
    EasingType EASE_IN_QUINT = registerSimple("easeinquint", pow(4));
    EasingType EASE_OUT_QUINT = registerSimple("easeoutquint", easeOut(pow(5)));
    EasingType EASE_IN_OUT_QUINT = registerSimple("easeinoutquint", easeInOut(pow(5)));
    EasingType EASE_IN_EXPO = registerSimple("easeinexpo", EasingType::exp);
    EasingType EASE_OUT_EXPO = registerSimple("easeoutexpo", easeOut(EasingType::exp));
    EasingType EASE_IN_OUT_EXPO = registerSimple("easeinoutexpo", easeInOut(EasingType::exp));
    EasingType EASE_IN_CIRC = registerSimple("easeincirc", EasingType::circle);
    EasingType EASE_OUT_CIRC = registerSimple("easeoutcirc", easeOut(EasingType::circle));
    EasingType EASE_IN_OUT_CIRC = registerSimple("easeinoutcirc", easeInOut(EasingType::circle));
    EasingType EASE_IN_BACK = register("easeinback", EasingType::back);
    EasingType EASE_OUT_BACK = register("easeoutback", arg -> easeOut(back(arg)));
    EasingType EASE_IN_OUT_BACK = register("easeinoutback", arg -> easeInOut(back(arg)));
    EasingType EASE_IN_ELASTIC = register("easeinelastic", EasingType::elastic);
    EasingType EASE_OUT_ELASTIC = register("easeoutelastic", arg -> easeOut(elastic(arg)));
    EasingType EASE_IN_OUT_ELASTIC = register("easeinoutelastic", arg -> easeInOut(elastic(arg)));
    EasingType EASE_IN_BOUNCE = register("easeinbounce", EasingType::bounce);
    EasingType EASE_OUT_BOUNCE = register("easeoutbounce", arg -> easeOut(bounce(arg)));
    EasingType EASE_IN_OUT_BOUNCE = register("easeinoutbounce", arg -> easeInOut(bounce(arg)));
    CatmullRomEasing CATMULLROM = register("catmullrom", new CatmullRomEasing());

    DoubleUnaryOperator buildTransformer(Double easingArg);

    default double apply(EasingState easingState, ControllerState controllerState) {
        if (easingState.delta() >= 1) return easingState.toValue();

        double delta = buildTransformer(easingState.getFirstEasingArg(controllerState))
            .applyAsDouble(easingState.delta());

        return lerp(delta, easingState.fromValue(), easingState.toValue());
    }

    default void modifyKeyframes(Keyframe[] keyframes, int currentFrameIndex, MathParser mathParser) {}

    static <T extends EasingType> T register(String name, T easingType) {
        EASING_TYPES.putIfAbsent(name, easingType);

        return easingType;
    }

    static EasingType registerSimple(String name, DoubleUnaryOperator function) {
        return register(name, ignored -> function);
    }

    static EasingType fromString(String name) {
        EasingType type = EASING_TYPES.get(name.toLowerCase(Locale.ROOT));

        return type == null ? LINEAR : type;
    }

    static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    static double linear(double value) {
        return value;
    }

    static double quadratic(double value) {
        return value * value;
    }

    static double cubic(double value) {
        return value * value * value;
    }

    static double sine(double value) {
        return 1 - Math.cos(value * Math.PI / 2d);
    }

    static double circle(double value) {
        return 1 - Math.sqrt(1 - value * value);
    }

    static double exp(double value) {
        return Math.pow(2, 10 * (value - 1));
    }

    static DoubleUnaryOperator easeOut(DoubleUnaryOperator function) {
        return value -> 1 - function.applyAsDouble(1 - value);
    }

    static DoubleUnaryOperator easeInOut(DoubleUnaryOperator function) {
        return value -> value < 0.5d ? function.applyAsDouble(value * 2d) / 2d
            : 1 - function.applyAsDouble((1 - value) * 2d) / 2d;
    }

    static DoubleUnaryOperator elastic(Double argument) {
        final double elasticity = argument == null ? 1 : argument;

        return value -> 1 - Math.pow(Math.cos(value * Math.PI / 2d), 3) * Math.cos(value * elasticity * Math.PI);
    }

    static DoubleUnaryOperator bounce(Double argument) {
        final double bounciness = argument == null ? 0.5d : argument;

        return value -> {
            double one = 121d / 16d * value * value;
            double two = 121d / 4d * bounciness * Math.pow(value - 6d / 11d, 2) + 1 - bounciness;
            double three = 121d * bounciness * bounciness * Math.pow(value - 9d / 11d, 2) + 1 - bounciness * bounciness;
            double four = 484d * bounciness * bounciness * bounciness * Math.pow(value - 10.5d / 11d, 2) + 1
                - bounciness * bounciness * bounciness;

            return Math.min(Math.min(one, two), Math.min(three, four));
        };
    }

    static DoubleUnaryOperator back(Double argument) {
        final double overshoot = argument == null ? 1.70158d : argument * 1.70158d;

        return value -> value * value * ((overshoot + 1) * value - overshoot);
    }

    static DoubleUnaryOperator pow(double exponent) {
        return value -> Math.pow(value, exponent);
    }

    /*
     * Step implementation copyright (c) 2015 Boris Chumichev, used under the MIT License.
     * Permission is granted to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies,
     * subject to retaining this notice. The software is provided without warranty of any kind.
     */
    static DoubleUnaryOperator step(Double argument) {
        double rawSteps = argument == null ? 2 : argument;

        if (rawSteps < 2) throw new IllegalArgumentException("Steps must be >= 2, got " + rawSteps);

        final int steps = (int) rawSteps;

        return value -> {
            if (value < 0) return 0;

            double stepLength = 1d / steps;
            double lastStep = (steps - 1) * stepLength;

            if (value > lastStep) return lastStep;

            int left = 0;
            int right = steps - 1;

            while (right - left != 1) {
                int test = left + (right - left) / 2;

                if (value >= test * stepLength) left = test;
                else right = test;
            }

            return left * stepLength;
        };
    }

    final class CatmullRomEasing implements EasingType {

        public static double getPointOnSpline(double delta, double p0, double p1, double p2, double p3) {
            return 0.5d * (2d * p1 + (p2 - p0) * delta
                + (2d * p0 - 5d * p1 + 4d * p2 - p3) * delta * delta
                + (3d * p1 - p0 - 3d * p2 + p3) * delta * delta * delta);
        }

        @Override
        public void modifyKeyframes(Keyframe[] keyframes, int currentFrameIndex, MathParser mathParser) {
            Keyframe frame = keyframes[currentFrameIndex];
            MathValue before = currentFrameIndex == 0 ? frame.startValue()
                : keyframes[currentFrameIndex - 1].endValue();
            MathValue after = currentFrameIndex + 1 >= keyframes.length ? frame.endValue()
                : keyframes[currentFrameIndex + 1].endValue();

            keyframes[currentFrameIndex] = new Keyframe(
                frame.startTime(),
                frame.length(),
                frame.startValue(),
                frame.endValue(),
                frame.easingType(),
                new MathValue[] { before, after });
        }

        @Override
        public DoubleUnaryOperator buildTransformer(Double easingArg) {
            return easeInOut(EasingType::linear);
        }

        @Override
        public double apply(EasingState easingState, ControllerState controllerState) {
            if (easingState.delta() >= 1) return easingState.toValue();

            MathValue[] arguments = easingState.easingArgs();

            if (arguments.length < 2) return EasingType.super.apply(easingState, controllerState);

            return getPointOnSpline(
                easingState.delta(),
                arguments[0].get(controllerState),
                easingState.fromValue(),
                easingState.toValue(),
                arguments[1].get(controllerState));
        }
    }
}
