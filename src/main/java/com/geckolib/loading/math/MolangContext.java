package com.geckolib.loading.math;

/** Supplies query values to a compiled Molang expression. */
@FunctionalInterface
public interface MolangContext {

    MolangContext EMPTY = variableName -> 0;

    double resolve(String variableName);
}
