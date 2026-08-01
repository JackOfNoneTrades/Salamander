package com.geckolib.cache.animation;

import java.util.HashSet;
import java.util.Set;

import com.geckolib.loading.math.value.Variable;

/** Baked transform tracks for one named model bone. */
public final class BoneAnimation {

    private final String boneName;
    private final KeyframeStack rotationKeyFrames;
    private final KeyframeStack positionKeyFrames;
    private final KeyframeStack scaleKeyFrames;

    public BoneAnimation(String boneName, KeyframeStack rotationKeyFrames, KeyframeStack positionKeyFrames,
        KeyframeStack scaleKeyFrames) {
        this.boneName = boneName;
        this.rotationKeyFrames = rotationKeyFrames;
        this.positionKeyFrames = positionKeyFrames;
        this.scaleKeyFrames = scaleKeyFrames;
    }

    public String boneName() {
        return this.boneName;
    }

    public KeyframeStack rotationKeyFrames() {
        return this.rotationKeyFrames;
    }

    public KeyframeStack positionKeyFrames() {
        return this.positionKeyFrames;
    }

    public KeyframeStack scaleKeyFrames() {
        return this.scaleKeyFrames;
    }

    public Set<Variable> getUsedVariables() {
        Set<Variable> variables = new HashSet<>();

        variables.addAll(this.rotationKeyFrames.getUsedVariables());
        variables.addAll(this.positionKeyFrames.getUsedVariables());
        variables.addAll(this.scaleKeyFrames.getUsedVariables());

        return variables;
    }
}
