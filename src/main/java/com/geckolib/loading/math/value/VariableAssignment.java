package com.geckolib.loading.math.value;

import java.util.Set;

import com.geckolib.animation.state.ControllerState;
import com.geckolib.loading.math.MathValue;

/** Assigns a value within one controller evaluation. */
public final class VariableAssignment implements MathValue {

    private final Variable variable;
    private final MathValue value;

    public VariableAssignment(Variable variable, MathValue value) {
        this.variable = variable;
        this.value = value;
    }

    @Override
    public double get(ControllerState controllerState) {
        if (controllerState == null) return 0;

        double result = this.value.get(controllerState);

        controllerState.setVariable(this.variable.name(), result);

        return 0;
    }

    @Override
    public Set<Variable> getUsedVariables() {
        return this.value.getUsedVariables();
    }
}
