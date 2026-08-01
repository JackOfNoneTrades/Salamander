package com.geckolib.animation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.object.PlayState;
import com.geckolib.animation.state.AnimationTest;

/** Controller created on demand for one stateless animation key. */
public class StatelessAnimationController extends AnimationController<GeoAnimatable> {

    private RawAnimation currentAnimation;

    public StatelessAnimationController(String name) {
        super(name, StatelessAnimationController::overrideStateHandler);
    }

    public void setCurrentAnimation(RawAnimation animation) {
        this.currentAnimation = animation;
    }

    private static PlayState overrideStateHandler(AnimationTest<GeoAnimatable> test) {
        if (test.controller() instanceof StatelessAnimationController) {
            RawAnimation animation = ((StatelessAnimationController) test.controller()).currentAnimation;

            if (animation != null) return test.setAndContinue(animation);
        }

        return PlayState.STOP;
    }
}
