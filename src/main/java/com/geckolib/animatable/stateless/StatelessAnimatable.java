package com.geckolib.animatable.stateless;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.StatelessAnimationController;

/** Alternate animation API that creates one controller on demand for each animation key. */
public interface StatelessAnimatable {

    default void playAnimation(String animation) {
        playAnimation(
            RawAnimation.begin()
                .thenPlay(animation));
    }

    default void playLoopingAnimation(String animation) {
        playAnimation(
            RawAnimation.begin()
                .thenLoop(animation));
    }

    default void playAndHoldAnimation(String animation) {
        playAnimation(
            RawAnimation.begin()
                .thenPlayAndHold(animation));
    }

    default void stopAnimation(RawAnimation animation) {
        stopAnimation(animationKey(animation));
    }

    void playAnimation(RawAnimation animation);

    void stopAnimation(String animation);

    default void handleClientAnimationPlay(GeoAnimatable animatable, long animatableId, RawAnimation animation) {
        AnimatableManager<GeoAnimatable> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(animatableId);
        String animationKey = animationKey(animation);
        AnimationController<GeoAnimatable> controller = manager.getAnimationControllers()
            .get(animationKey);

        if (controller == null) {
            controller = new StatelessAnimationController(animationKey);
            manager.addController(controller);
        }

        if (controller instanceof StatelessAnimationController)
            ((StatelessAnimationController) controller).setCurrentAnimation(animation);
    }

    default void handleClientAnimationStop(GeoAnimatable animatable, long animatableId, String animation) {
        AnimatableManager<GeoAnimatable> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(animatableId);
        AnimationController<GeoAnimatable> controller = manager.getAnimationControllers()
            .get(animation);

        if (controller instanceof StatelessAnimationController)
            ((StatelessAnimationController) controller).setCurrentAnimation(null);
    }

    static String animationKey(RawAnimation animation) {
        if (animation == null) throw new IllegalArgumentException("Stateless animation cannot be null");

        return animation.getStageCount() == 1 ? animation.getAnimationStages()
            .get(0)
            .animationName() : animation.toString();
    }
}
