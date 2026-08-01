package com.geckolib.animation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.geckolib.animation.object.EasingType;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ControllerState;
import com.geckolib.animation.state.EasingState;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BoneAnimation;
import com.geckolib.cache.animation.Keyframe;
import com.geckolib.loading.math.MolangContext;

/** CPU-only animation evaluation. Rendering is layered on these snapshots by the client module. */
public final class AnimationProcessor {

    private AnimationProcessor() {}

    public static Map<String, BoneSnapshot> evaluate(Animation animation, double animationTime,
        MolangContext molangContext) {
        AnimationPoint point = AnimationPoint.createFor(animation, null, animation.loopType(), animationTime);
        ControllerState state = new ControllerState(molangContext).setAnimationTime(point.animTime());

        return evaluate(point, state);
    }

    public static Map<String, BoneSnapshot> evaluate(AnimationPoint animationPoint, ControllerState controllerState) {
        BoneAnimation[] boneAnimations = animationPoint.animation()
            .boneAnimations();
        Map<String, BoneSnapshot> snapshots = new LinkedHashMap<>(boneAnimations.length);

        controllerState.setAnimationTime(animationPoint.animTime());

        for (int boneIndex = 0; boneIndex < boneAnimations.length; boneIndex++) {
            BoneAnimation boneAnimation = boneAnimations[boneIndex];
            BoneSnapshot snapshot = BoneSnapshot.create(boneAnimation.boneName());

            snapshot.setScale(
                findAnimationPointValue(
                    controllerState,
                    animationPoint,
                    boneIndex,
                    AnimationPoint.Transform.SCALE,
                    AnimationPoint.Axis.X),
                findAnimationPointValue(
                    controllerState,
                    animationPoint,
                    boneIndex,
                    AnimationPoint.Transform.SCALE,
                    AnimationPoint.Axis.Y),
                findAnimationPointValue(
                    controllerState,
                    animationPoint,
                    boneIndex,
                    AnimationPoint.Transform.SCALE,
                    AnimationPoint.Axis.Z));
            snapshot.setRotation(
                findAnimationPointValue(
                    controllerState,
                    animationPoint,
                    boneIndex,
                    AnimationPoint.Transform.ROTATION,
                    AnimationPoint.Axis.X),
                findAnimationPointValue(
                    controllerState,
                    animationPoint,
                    boneIndex,
                    AnimationPoint.Transform.ROTATION,
                    AnimationPoint.Axis.Y),
                findAnimationPointValue(
                    controllerState,
                    animationPoint,
                    boneIndex,
                    AnimationPoint.Transform.ROTATION,
                    AnimationPoint.Axis.Z));
            snapshot.setTranslation(
                findAnimationPointValue(
                    controllerState,
                    animationPoint,
                    boneIndex,
                    AnimationPoint.Transform.TRANSLATION,
                    AnimationPoint.Axis.X),
                findAnimationPointValue(
                    controllerState,
                    animationPoint,
                    boneIndex,
                    AnimationPoint.Transform.TRANSLATION,
                    AnimationPoint.Axis.Y),
                findAnimationPointValue(
                    controllerState,
                    animationPoint,
                    boneIndex,
                    AnimationPoint.Transform.TRANSLATION,
                    AnimationPoint.Axis.Z));

            snapshots.put(boneAnimation.boneName(), snapshot);
        }

        return Collections.unmodifiableMap(snapshots);
    }

    public static float findAnimationPointValue(ControllerState controllerState, AnimationPoint animationPoint,
        int boneIndex, AnimationPoint.Transform transform, AnimationPoint.Axis axis) {
        Keyframe fromKeyframe = animationPoint.getCurrentKeyframe(boneIndex, transform, axis);
        Keyframe toKeyframe = animationPoint.getNextKeyframe(boneIndex, transform, axis);

        if (fromKeyframe == null || toKeyframe == null) return transform.defaultValue;

        double from = fromKeyframe.endValue()
            .get(controllerState);
        double to = toKeyframe.endValue()
            .get(controllerState);
        double delta = toKeyframe.length() == 0 ? 0
            : (animationPoint.animTime() - fromKeyframe.startTime()) / toKeyframe.length();
        EasingType easingType = animationPoint.easingOverride() == null ? toKeyframe.easingType()
            : animationPoint.easingOverride();
        EasingState easingState = new EasingState(easingType, toKeyframe.easingArgs(), delta, from, to);

        return (float) easingState.interpolate(controllerState);
    }
}
