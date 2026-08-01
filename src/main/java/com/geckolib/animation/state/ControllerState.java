package com.geckolib.animation.state;

import java.util.HashMap;
import java.util.Map;

import com.geckolib.loading.math.MolangContext;
import com.geckolib.loading.math.value.Variable;

/**
 * Headless state used while evaluating an animation.
 *
 * <p>
 * The modern GeckoLib render-state fields will be added by the renderer milestone. Molang variables are already
 * scoped to this state so concurrent entity evaluations cannot affect each other.
 */
public final class ControllerState {

    private final MolangContext molangContext;
    private final Map<String, Double> localVariables = new HashMap<>();
    private double animationTime;

    public ControllerState() {
        this(MolangContext.EMPTY);
    }

    public ControllerState(MolangContext molangContext) {
        this.molangContext = molangContext == null ? MolangContext.EMPTY : molangContext;
    }

    public double getAnimationTime() {
        return this.animationTime;
    }

    public ControllerState setAnimationTime(double animationTime) {
        this.animationTime = animationTime;

        return this;
    }

    public double getQueryValue(Variable variable) {
        return resolveVariable(variable.name());
    }

    public double resolveVariable(String variableName) {
        String normalizedName = Variable.normalizeName(variableName);
        Double localValue = this.localVariables.get(normalizedName);

        if (localValue != null) return localValue;

        if ("query.anim_time".equals(normalizedName)) return this.animationTime;

        return this.molangContext.resolve(normalizedName);
    }

    public void setVariable(String variableName, double value) {
        this.localVariables.put(Variable.normalizeName(variableName), value);
    }
}
