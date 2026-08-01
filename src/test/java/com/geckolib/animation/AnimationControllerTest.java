package com.geckolib.animation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.AnimationPoint;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.constant.DataTickets;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.util.GeckoLibUtil;

public class AnimationControllerTest {

    private static final double EPSILON = 1.0E-5;
    private static final RawAnimation WALK = RawAnimation.begin()
        .thenLoop("animation.test.walk");

    @Test
    public void advancesAndLoopsUsingMinecraftTicks() throws Exception {
        TestAnimatable animatable = new TestAnimatable();
        AnimatableManager<TestAnimatable> manager = animatable.cache.getManagerForId(1);
        BakedAnimations animations = loadFixture();

        manager.setAnimatableData(DataTickets.IS_MOVING, true);
        assertEquals(Boolean.TRUE, manager.getAnimatableData(DataTickets.IS_MOVING));

        AnimationPoint initial = animatable.controller.tick(animatable, manager, animations, 0, MolangContext.EMPTY);

        assertNotNull(initial);
        assertEquals(0, initial.animTime(), EPSILON);

        animatable.controller.tick(animatable, manager, animations, 10, MolangContext.EMPTY);

        BoneSnapshot halfway = animatable.controller.evaluateCurrentPose(MolangContext.EMPTY)
            .get("body");

        assertEquals(0.5, animatable.controller.getCurrentAnimationTime(), EPSILON);
        assertEquals(1, halfway.getTranslateX(), EPSILON);

        animatable.controller.tick(animatable, manager, animations, 20, MolangContext.EMPTY);

        assertEquals(0, animatable.controller.getCurrentAnimationTime(), EPSILON);
    }

    private BakedAnimations loadFixture() throws Exception {
        try (Reader reader = new InputStreamReader(
            getClass().getResourceAsStream("/animations/headless.animation.json"),
            StandardCharsets.UTF_8)) {
            return new GeckoLibGsonLoader().loadAnimations(reader);
        }
    }

    private static final class TestAnimatable implements GeoAnimatable {

        private final AnimationController<TestAnimatable> controller = new AnimationController<>(
            test -> test.setAndContinue(WALK));
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(this.controller);
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
    }
}
