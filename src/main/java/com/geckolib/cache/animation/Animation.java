package com.geckolib.cache.animation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.cache.animation.keyframeevent.CustomInstructionKeyframeData;
import com.geckolib.cache.animation.keyframeevent.ParticleKeyframeData;
import com.geckolib.cache.animation.keyframeevent.SoundKeyframeData;
import com.geckolib.loading.math.value.Variable;

/** Immutable baked animation. */
public final class Animation {

    private final String name;
    private final double length;
    private final LoopType loopType;
    private final BoneAnimation[] boneAnimations;
    private final Set<Variable> usedVariables;
    private final KeyframeMarkers keyframeMarkers;

    public Animation(String name, double length, LoopType loopType, BoneAnimation[] boneAnimations,
        Set<Variable> usedVariables, KeyframeMarkers keyframeMarkers) {
        this.name = name;
        this.length = length;
        this.loopType = loopType;
        this.boneAnimations = boneAnimations;
        this.usedVariables = usedVariables;
        this.keyframeMarkers = keyframeMarkers;
    }

    public String name() {
        return this.name;
    }

    public double length() {
        return this.length;
    }

    public LoopType loopType() {
        return this.loopType;
    }

    public BoneAnimation[] boneAnimations() {
        return this.boneAnimations;
    }

    public Set<Variable> usedVariables() {
        return this.usedVariables;
    }

    public KeyframeMarkers keyframeMarkers() {
        return this.keyframeMarkers;
    }

    public static Animation create(String name, double length, LoopType loopType, BoneAnimation[] boneAnimations,
        KeyframeMarkers keyframeMarkers) {
        Set<Variable> variables = new HashSet<>();

        for (BoneAnimation boneAnimation : boneAnimations) {
            variables.addAll(boneAnimation.getUsedVariables());
        }

        return new Animation(
            name,
            length,
            loopType,
            boneAnimations,
            Collections.unmodifiableSet(variables),
            keyframeMarkers);
    }

    public static Animation generateWaitAnimation(double length) {
        return create(RawAnimation.Stage.WAIT, length, LoopType.PLAY_ONCE, new BoneAnimation[0], KeyframeMarkers.EMPTY);
    }

    public static final class KeyframeMarkers {

        public static final KeyframeMarkers EMPTY = new KeyframeMarkers(
            new SoundKeyframeData[0],
            new ParticleKeyframeData[0],
            new CustomInstructionKeyframeData[0]);

        private final SoundKeyframeData[] sounds;
        private final ParticleKeyframeData[] particles;
        private final CustomInstructionKeyframeData[] customInstructions;

        public KeyframeMarkers(SoundKeyframeData[] sounds, ParticleKeyframeData[] particles,
            CustomInstructionKeyframeData[] customInstructions) {
            this.sounds = sounds;
            this.particles = particles;
            this.customInstructions = customInstructions;
        }

        public SoundKeyframeData[] sounds() {
            return this.sounds;
        }

        public ParticleKeyframeData[] particles() {
            return this.particles;
        }

        public CustomInstructionKeyframeData[] customInstructions() {
            return this.customInstructions;
        }

        public boolean isEmpty() {
            return this.sounds.length == 0 && this.particles.length == 0 && this.customInstructions.length == 0;
        }
    }
}
