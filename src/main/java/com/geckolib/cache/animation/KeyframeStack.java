package com.geckolib.cache.animation;

import java.util.HashSet;
import java.util.Set;

import com.geckolib.loading.math.value.Variable;

/** Ordered keyframe arrays for the three axes of one transform. */
public final class KeyframeStack {

    public static final KeyframeStack EMPTY = new KeyframeStack(new Keyframe[0], new Keyframe[0], new Keyframe[0]);

    private final Keyframe[] xKeyframes;
    private final Keyframe[] yKeyframes;
    private final Keyframe[] zKeyframes;

    public KeyframeStack(Keyframe[] xKeyframes, Keyframe[] yKeyframes, Keyframe[] zKeyframes) {
        this.xKeyframes = xKeyframes;
        this.yKeyframes = yKeyframes;
        this.zKeyframes = zKeyframes;
    }

    public Keyframe[] xKeyframes() {
        return this.xKeyframes;
    }

    public Keyframe[] yKeyframes() {
        return this.yKeyframes;
    }

    public Keyframe[] zKeyframes() {
        return this.zKeyframes;
    }

    public Set<Variable> getUsedVariables() {
        Set<Variable> variables = new HashSet<>();

        collectVariables(this.xKeyframes, variables);
        collectVariables(this.yKeyframes, variables);
        collectVariables(this.zKeyframes, variables);

        return variables;
    }

    public double getTotalKeyframeTime() {
        return Math
            .max(lastEndTime(this.xKeyframes), Math.max(lastEndTime(this.yKeyframes), lastEndTime(this.zKeyframes)));
    }

    private static void collectVariables(Keyframe[] keyframes, Set<Variable> variables) {
        for (Keyframe keyframe : keyframes) {
            variables.addAll(keyframe.getUsedVariables());
        }
    }

    private static double lastEndTime(Keyframe[] keyframes) {
        if (keyframes.length == 0) return 0;

        Keyframe keyframe = keyframes[keyframes.length - 1];

        return keyframe.startTime() + keyframe.length();
    }
}
