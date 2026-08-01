package com.geckolib.animation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private static final RawAnimation ZERO_MARKER = RawAnimation.begin()
        .thenLoop("animation.test.marker_zero");

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

    @Test
    public void transitionsIntoAndOutOfAnimationPose() throws Exception {
        TransitionAnimatable animatable = new TransitionAnimatable();
        AnimatableManager<TransitionAnimatable> manager = animatable.cache.getManagerForId(2);
        BakedAnimations animations = loadFixture();

        animatable.controller.tick(animatable, manager, animations, 0, MolangContext.EMPTY);
        assertTrue(animatable.controller.isTransitioning());
        assertEquals(
            0,
            animatable.controller.evaluateCurrentPose(MolangContext.EMPTY)
                .get("body")
                .getTranslateX(),
            EPSILON);

        animatable.controller.tick(animatable, manager, animations, 10, MolangContext.EMPTY);
        assertFalse(animatable.controller.isTransitioning());
        assertEquals(
            1,
            animatable.controller.evaluateCurrentPose(MolangContext.EMPTY)
                .get("body")
                .getTranslateX(),
            EPSILON);

        animatable.playing = false;
        animatable.controller.tick(animatable, manager, animations, 10, MolangContext.EMPTY);
        assertTrue(animatable.controller.isTransitioning());
        assertEquals(
            1,
            animatable.controller.evaluateCurrentPose(MolangContext.EMPTY)
                .get("body")
                .getTranslateX(),
            EPSILON);

        animatable.controller.tick(animatable, manager, animations, 15, MolangContext.EMPTY);
        assertEquals(
            0.5,
            animatable.controller.evaluateCurrentPose(MolangContext.EMPTY)
                .get("body")
                .getTranslateX(),
            EPSILON);

        animatable.controller.tick(animatable, manager, animations, 20, MolangContext.EMPTY);
        assertFalse(animatable.controller.isTransitioning());
        assertTrue(
            animatable.controller.evaluateCurrentPose(MolangContext.EMPTY)
                .isEmpty());
    }

    @Test
    public void triggersAndStopsOnlyMatchingRegisteredAnimations() {
        TriggerAnimatable animatable = new TriggerAnimatable();
        AnimatableManager<TriggerAnimatable> manager = animatable.cache.getManagerForId(3);

        assertFalse(manager.tryTriggerAnimation("missing"));
        assertTrue(manager.tryTriggerAnimation("action", "attack"));
        assertTrue(animatable.actionController.isTriggeredAnimation("attack"));
        assertFalse(animatable.actionController.isTriggeredAnimation("roar"));
        assertFalse(manager.stopTriggeredAnimation("action", "roar"));
        assertEquals(WALK, animatable.actionController.getCurrentRawAnimation());
        assertTrue(manager.stopTriggeredAnimation("action", "attack"));
        assertFalse(animatable.actionController.isTriggeredAnimation("attack"));

        assertTrue(manager.tryTriggerAnimation("roar"));
        assertTrue(animatable.voiceController.isTriggeredAnimation("roar"));
        assertTrue(manager.stopTriggeredAnimation((String) null));
        assertFalse(animatable.voiceController.isTriggeredAnimation("roar"));
    }

    @Test
    public void dispatchesKeyframeMarkersOncePerLoopCrossing() throws Exception {
        EventAnimatable animatable = new EventAnimatable();
        AnimatableManager<EventAnimatable> manager = animatable.cache.getManagerForId(4);
        BakedAnimations animations = loadFixture();

        animatable.controller.tick(animatable, manager, animations, 0, MolangContext.EMPTY);
        animatable.controller.tick(animatable, manager, animations, 5.5, MolangContext.EMPTY);
        animatable.controller.tick(animatable, manager, animations, 10.5, MolangContext.EMPTY);
        animatable.controller.tick(animatable, manager, animations, 15.5, MolangContext.EMPTY);

        assertEquals(
            Arrays.asList("sound:test.step", "particle:test.dust", "custom:test_instruction"),
            animatable.events);
        assertEquals(0.5f, animatable.partialTick, EPSILON);

        animatable.controller.tick(animatable, manager, animations, 25.5, MolangContext.EMPTY);

        assertEquals(
            Arrays.asList("sound:test.step", "particle:test.dust", "custom:test_instruction", "sound:test.step"),
            animatable.events);
    }

    @Test
    public void dispatchesAllMarkersCrossedByOneLargeAdvance() throws Exception {
        EventAnimatable animatable = new EventAnimatable();
        AnimatableManager<EventAnimatable> manager = animatable.cache.getManagerForId(5);
        BakedAnimations animations = loadFixture();

        animatable.controller.tick(animatable, manager, animations, 0, MolangContext.EMPTY);
        animatable.controller.tick(animatable, manager, animations, 20, MolangContext.EMPTY);

        assertEquals(
            Arrays.asList("sound:test.step", "particle:test.dust", "custom:test_instruction"),
            animatable.events);
    }

    @Test
    public void dispatchesStartMarkersOnceForEachLoop() throws Exception {
        ZeroMarkerAnimatable animatable = new ZeroMarkerAnimatable();
        AnimatableManager<ZeroMarkerAnimatable> manager = animatable.cache.getManagerForId(6);
        BakedAnimations animations = loadFixture();

        animatable.controller.tick(animatable, manager, animations, 0, MolangContext.EMPTY);
        animatable.controller.tick(animatable, manager, animations, 1, MolangContext.EMPTY);
        assertEquals(1, animatable.events);

        animatable.controller.tick(animatable, manager, animations, 20, MolangContext.EMPTY);
        assertEquals(1, animatable.events);

        animatable.controller.tick(animatable, manager, animations, 21, MolangContext.EMPTY);
        assertEquals(2, animatable.events);
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

    private static final class TransitionAnimatable implements GeoAnimatable {

        private boolean playing = true;
        private final AnimationController<TransitionAnimatable> controller = new AnimationController<>(
            "transition",
            10,
            test -> this.playing ? test.setAndContinue(WALK) : com.geckolib.animation.object.PlayState.STOP);
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

    private static final class TriggerAnimatable implements GeoAnimatable {

        private final AnimationController<TriggerAnimatable> actionController = new AnimationController<>(
            "action",
            test -> com.geckolib.animation.object.PlayState.STOP);
        private final AnimationController<TriggerAnimatable> voiceController = new AnimationController<>(
            "voice",
            test -> com.geckolib.animation.object.PlayState.STOP);
        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        private TriggerAnimatable() {
            this.actionController.triggerableAnim("attack", WALK);
            this.voiceController.triggerableAnim("roar", WALK);
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
            controllers.add(this.actionController, this.voiceController);
        }

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
    }

    private static final class EventAnimatable implements GeoAnimatable {

        private final List<String> events = new ArrayList<>();
        private float partialTick;
        private final AnimationController<EventAnimatable> controller = new AnimationController<EventAnimatable>(
            test -> test.setAndContinue(WALK)).setSoundKeyframeHandler(event -> {
                this.events.add(
                    "sound:" + event.keyframeData()
                        .getSound());
                this.partialTick = event.getPartialTick();
                assertEquals(this, event.animatable());
                assertEquals(this.controller, event.controller());
            })
                .setParticleKeyframeHandler(
                    event -> this.events.add(
                        "particle:" + event.keyframeData()
                            .getEffect()))
                .setCustomInstructionKeyframeHandler(
                    event -> this.events.add(
                        "custom:" + event.keyframeData()
                            .getInstructions()));
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

    private static final class ZeroMarkerAnimatable implements GeoAnimatable {

        private int events;
        private final AnimationController<ZeroMarkerAnimatable> controller = new AnimationController<ZeroMarkerAnimatable>(
            test -> test.setAndContinue(ZERO_MARKER)).setCustomInstructionKeyframeHandler(event -> this.events++);
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
