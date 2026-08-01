package com.geckolib.loading.math.value;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;

/** A named Molang variable. */
public final class Variable implements MathValue {

    private final String name;
    private final AtomicReference<ToDoubleFunction<ControllerState>> value;

    public Variable(String name) {
        this(name, state -> state == null ? 0 : state.resolveVariable(normalizeName(name)));
    }

    public Variable(String name, double value) {
        this(name, state -> value);
    }

    public Variable(String name, ToDoubleFunction<ControllerState> value) {
        this.name = normalizeName(name);
        this.value = new AtomicReference<>(value);
    }

    public String name() {
        return this.name;
    }

    @Override
    public double get(ControllerState controllerState) {
        if (controllerState == null) return 0;

        return this.value.get()
            .applyAsDouble(controllerState);
    }

    public void set(double value) {
        this.value.set(state -> value);
    }

    public void set(ToDoubleFunction<ControllerState> value) {
        this.value.set(value);
    }

    @Override
    public Set<Variable> getUsedVariables() {
        return Collections.singleton(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Variable && this.name.equals(((Variable) obj).name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
        return "variable(" + this.name + ")";
    }

    public static String normalizeName(String name) {
        String normalized = name.toLowerCase(Locale.ROOT);

        if (normalized.startsWith("q.")) return "query." + normalized.substring(2);

        if (normalized.startsWith("v.")) return "variable." + normalized.substring(2);

        if (normalized.startsWith("t.")) return "temp." + normalized.substring(2);

        return normalized;
    }
}
