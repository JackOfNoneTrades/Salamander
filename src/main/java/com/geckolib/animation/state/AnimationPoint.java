package com.geckolib.animation.state;

import java.util.function.Function;

import com.geckolib.animation.object.EasingType;
import com.geckolib.animation.object.LoopType;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.animation.Keyframe;
import com.geckolib.cache.animation.KeyframeStack;

/** Keyframe indices and animation-relative time for one baked animation. */
public final class AnimationPoint {

    public static final int NO_KEYFRAME = -2;
    public static final int BEFORE_FIRST_KEYFRAME = -1;

    private final Animation animation;
    private final EasingType easingOverride;
    private final LoopType loopType;
    private final double animTime;
    private final int[][][] keyFramePoints;

    public AnimationPoint(Animation animation, EasingType easingOverride, LoopType loopType, double animTime,
        int[][][] keyFramePoints) {
        this.animation = animation;
        this.easingOverride = easingOverride;
        this.loopType = loopType;
        this.animTime = animTime;
        this.keyFramePoints = keyFramePoints;
    }

    public Animation animation() {
        return this.animation;
    }

    public EasingType easingOverride() {
        return this.easingOverride;
    }

    public LoopType loopType() {
        return this.loopType;
    }

    public double animTime() {
        return this.animTime;
    }

    public int[][][] keyFramePoints() {
        return this.keyFramePoints;
    }

    public boolean hasFinished() {
        return Math.abs(this.animTime - this.animation.length()) < 1.0E-5;
    }

    public Keyframe getPreviousKeyframe(int boneIndex, Transform transform, Axis axis) {
        return getKeyframe(boneIndex, transform, axis, -1);
    }

    public Keyframe getCurrentKeyframe(int boneIndex, Transform transform, Axis axis) {
        return getKeyframe(boneIndex, transform, axis, 0);
    }

    public Keyframe getNextKeyframe(int boneIndex, Transform transform, Axis axis) {
        return getKeyframe(boneIndex, transform, axis, 1);
    }

    public Keyframe getKeyframe(int boneIndex, Transform transform, Axis axis, int offset) {
        Keyframe[] keyframes = axis.keyframes(transform.keyframeStack(this.animation.boneAnimations()[boneIndex]));
        int keyframeIndex = this.keyFramePoints[boneIndex][transform.index][axis.index];

        if (keyframes.length == 0 || keyframeIndex == NO_KEYFRAME) return null;

        return keyframes[clamp(keyframeIndex + offset, 0, keyframes.length - 1)];
    }

    public AnimationPoint createNext(double animationTime) {
        if (Math.abs(animationTime - this.animTime) < 1.0E-5) return this;

        double clampedTime = clamp(animationTime, 0, this.animation.length());
        int[][][] points = constructBoneArray(this.animation, clampedTime);

        return new AnimationPoint(this.animation, this.easingOverride, this.loopType, clampedTime, points);
    }

    public int findBoneIndex(String boneName) {
        BoneAnimation[] animations = this.animation.boneAnimations();

        for (int i = 0; i < animations.length; i++) {
            if (animations[i].boneName()
                .equals(boneName)) return i;
        }

        return -1;
    }

    public static AnimationPoint createFor(Animation animation, EasingType easingOverride, LoopType loopType,
        double animationTime) {
        double clampedTime = clamp(animationTime, 0, animation.length());

        return new AnimationPoint(
            animation,
            easingOverride,
            loopType,
            clampedTime,
            constructBoneArray(animation, clampedTime));
    }

    private static int[][][] constructBoneArray(Animation animation, double animationTime) {
        BoneAnimation[] boneAnimations = animation.boneAnimations();
        int[][][] bones = new int[boneAnimations.length][3][3];

        for (int i = 0; i < boneAnimations.length; i++) {
            findKeyframePoints(boneAnimations[i].scaleKeyFrames(), bones[i][Transform.SCALE.index], animationTime);
            findKeyframePoints(
                boneAnimations[i].rotationKeyFrames(),
                bones[i][Transform.ROTATION.index],
                animationTime);
            findKeyframePoints(
                boneAnimations[i].positionKeyFrames(),
                bones[i][Transform.TRANSLATION.index],
                animationTime);
        }

        return bones;
    }

    private static void findKeyframePoints(KeyframeStack stack, int[] axisPoints, double animationTime) {
        axisPoints[Axis.X.index] = findKeyframePoint(stack.xKeyframes(), animationTime);
        axisPoints[Axis.Y.index] = findKeyframePoint(stack.yKeyframes(), animationTime);
        axisPoints[Axis.Z.index] = findKeyframePoint(stack.zKeyframes(), animationTime);
    }

    private static int findKeyframePoint(Keyframe[] keyframes, double animationTime) {
        if (keyframes.length == 0) return NO_KEYFRAME;

        if (keyframes[0].startTime() > animationTime) return BEFORE_FIRST_KEYFRAME;

        for (int i = 0; i < keyframes.length; i++) {
            if (i + 1 < keyframes.length && keyframes[i + 1].startTime() >= animationTime) return i;
        }

        return keyframes.length - 1;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public enum Transform {

        SCALE(0, 1, BoneAnimation::scaleKeyFrames),
        ROTATION(1, 0, BoneAnimation::rotationKeyFrames),
        TRANSLATION(2, 0, BoneAnimation::positionKeyFrames);

        public final int index;
        public final float defaultValue;
        private final Function<BoneAnimation, KeyframeStack> stackFunction;

        Transform(int index, float defaultValue, Function<BoneAnimation, KeyframeStack> stackFunction) {
            this.index = index;
            this.defaultValue = defaultValue;
            this.stackFunction = stackFunction;
        }

        public KeyframeStack keyframeStack(BoneAnimation boneAnimation) {
            return this.stackFunction.apply(boneAnimation);
        }
    }

    public enum Axis {

        X(0, KeyframeStack::xKeyframes),
        Y(1, KeyframeStack::yKeyframes),
        Z(2, KeyframeStack::zKeyframes);

        public final int index;
        private final Function<KeyframeStack, Keyframe[]> framesFunction;

        Axis(int index, Function<KeyframeStack, Keyframe[]> framesFunction) {
            this.index = index;
            this.framesFunction = framesFunction;
        }

        public Keyframe[] keyframes(KeyframeStack keyframeStack) {
            return this.framesFunction.apply(keyframeStack);
        }
    }
}
