package com.geckolib.animatable.stateless;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.StatelessAnimationController;
import com.geckolib.animation.object.PlayState;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.util.GeckoLibUtil;

public class StatelessAnimatableTest {

    @Test
    public void createsPlaysAndStopsAControllerWithoutReplacingRegisteredControllers() throws Exception {
        TestAnimatable animatable = new TestAnimatable();
        AnimatableManager<GeoAnimatable> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(12);
        RawAnimation animation = RawAnimation.begin()
            .thenLoop("animation.test.walk");

        animatable.playAnimation(animation);

        AnimationController<GeoAnimatable> controller = manager.getAnimationControllers()
            .get("animation.test.walk");

        assertTrue(controller instanceof StatelessAnimationController);
        assertSame(
            animatable.movementController,
            manager.getAnimationControllers()
                .get("movement"));

        controller.tick(animatable, manager, loadFixture(), 0, MolangContext.EMPTY);
        assertEquals(animation, controller.getCurrentRawAnimation());

        animatable.stopAnimation(animation);
        controller.tick(animatable, manager, loadFixture(), 1, MolangContext.EMPTY);
        assertNull(controller.getCurrentRawAnimation());
    }

    @Test
    public void usesDeterministicKeysForMultiStageAnimations() {
        TestAnimatable animatable = new TestAnimatable();
        AnimatableManager<GeoAnimatable> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(3);
        RawAnimation animation = RawAnimation.begin()
            .thenPlay("animation.test.walk")
            .thenWait(5);

        animatable.playAnimation(animation);

        String expectedKey = "RawAnimation{animation.test.walk -> internal.wait}";

        assertEquals(expectedKey, StatelessAnimatable.animationKey(animation));
        assertTrue(
            manager.getAnimationControllers()
                .get(expectedKey) instanceof StatelessAnimationController);
    }

    private BakedAnimations loadFixture() throws Exception {
        try (Reader reader = new InputStreamReader(
            getClass().getResourceAsStream("/animations/headless.animation.json"),
            StandardCharsets.UTF_8)) {
            return new GeckoLibGsonLoader().loadAnimations(reader);
        }
    }

    private static final class TestAnimatable implements StatelessGeoObject {

        private final AnimationController<TestAnimatable> movementController = new AnimationController<>(
            "movement",
            test -> PlayState.STOP);
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void playAnimation(RawAnimation animation) {
            handleClientAnimationPlay(this, 0, animation);
        }

        @Override
        public void stopAnimation(String animation) {
            handleClientAnimationStop(this, 0, animation);
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(this.movementController);
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
    }
}
