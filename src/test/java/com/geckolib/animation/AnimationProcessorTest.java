package com.geckolib.animation;

import static org.junit.Assert.assertEquals;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import org.junit.Test;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoLocator;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.util.GeckoLibUtil;

public class AnimationProcessorTest {

    private static final double EPSILON = 1.0E-5;
    private static final RawAnimation WALK = RawAnimation.begin()
        .thenLoop("animation.test.walk");

    @Test
    public void appliesOrderedAndAdditiveControllersToFreshPose() throws Exception {
        TestAnimatable animatable = new TestAnimatable();
        AnimatableManager<TestAnimatable> manager = animatable.cache.getManagerForId(3);
        BakedAnimations animations = loadAnimations();
        BakedGeoModel model = createModel();

        AnimationProcessor.createModelPose(animatable, manager, animations, model, 0, MolangContext.EMPTY);

        ModelPose pose = AnimationProcessor
            .createModelPose(animatable, manager, animations, model, 10, MolangContext.EMPTY);

        assertEquals(
            2,
            pose.get("body")
                .get()
                .getTranslateX(),
            EPSILON);
        assertEquals(
            1.5625,
            pose.get("body")
                .get()
                .getScaleX(),
            EPSILON);
    }

    private static BakedAnimations loadAnimations() throws Exception {
        try (Reader reader = new InputStreamReader(
            AnimationProcessorTest.class.getResourceAsStream("/animations/headless.animation.json"),
            StandardCharsets.UTF_8)) {
            return new GeckoLibGsonLoader().loadAnimations(reader);
        }
    }

    private static BakedGeoModel createModel() {
        GeoBone body = new CuboidGeoBone(
            null,
            "body",
            new GeoBone[0],
            new GeoCube[0],
            new GeoLocator[0],
            0,
            0,
            0,
            0,
            0,
            0);

        return new BakedGeoModel(new GeoBone[] { body }, Collections.emptyMap(), null);
    }

    private static final class TestAnimatable implements GeoAnimatable {

        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(new AnimationController<TestAnimatable>(test -> test.setAndContinue(WALK)));
            controllers.add(
                new AnimationController<TestAnimatable>("additive", test -> test.setAndContinue(WALK))
                    .additiveAnimations());
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
    }
}
