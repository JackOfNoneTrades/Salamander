package com.geckolib.loading.math;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.ToDoubleFunction;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.value.Variable;

/** Base interface for compiled Molang and mathematical values. */
public interface MathValue extends ToDoubleFunction<ControllerState> {

    double get(ControllerState controllerState);

    default boolean isMutable() {
        return true;
    }

    default Set<Variable> getUsedVariables() {
        return Collections.emptySet();
    }

    @Override
    default double applyAsDouble(ControllerState controllerState) {
        return get(controllerState);
    }

    static Set<Variable> collectUsedVariables(MathValue... values) {
        if (values.length == 0) return Collections.emptySet();

        if (values.length == 1) return values[0].getUsedVariables();

        Set<Variable> usedVariables = new HashSet<>();

        for (MathValue value : values) {
            usedVariables.addAll(value.getUsedVariables());
        }

        return Collections.unmodifiableSet(usedVariables);
    }
}
