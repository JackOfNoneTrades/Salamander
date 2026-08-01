package com.geckolib.loading.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Test;

import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.loading.math.MolangContext;

public class GeckoLibGsonLoaderTest {

    private static final double EPSILON = 1.0E-5;

    @Test
    public void loadsAndEvaluatesGeckoLibFiveAnimationJson() throws Exception {
        BakedAnimations animations = loadFixture();
        Animation animation = animations.getAnimation("animation.test.walk");

        assertNotNull(animation);
        assertEquals(1, animation.length(), EPSILON);
        assertSame(LoopType.LOOP, animation.loopType());
        assertEquals(
            "test.step",
            animation.keyframeMarkers()
                .sounds()[0].getSound());
        assertEquals(
            "test.dust",
            animation.keyframeMarkers()
                .particles()[0].getEffect());
        assertEquals(
            "test_instruction",
            animation.keyframeMarkers()
                .customInstructions()[0].getInstructions());

        Map<String, BoneSnapshot> snapshots = AnimationProcessor.evaluate(animation, 0.5, MolangContext.EMPTY);
        BoneSnapshot body = snapshots.get("body");

        assertNotNull(body);
        assertEquals(1, body.getTranslateX(), EPSILON);
        assertEquals(1, body.getTranslateY(), EPSILON);
        assertEquals(1.5, body.getTranslateZ(), EPSILON);
        assertEquals(-Math.PI / 4, body.getRotX(), EPSILON);
        assertEquals(Math.PI / 4, body.getRotY(), EPSILON);
        assertEquals(Math.PI / 2, body.getRotZ(), EPSILON);
        assertEquals(1.25, body.getScaleX(), EPSILON);
        assertEquals(1.5, body.getScaleY(), EPSILON);
        assertEquals(1.75, body.getScaleZ(), EPSILON);
    }

    @Test
    public void appliesCatmullRomInterpolation() throws Exception {
        Animation animation = loadFixture().getAnimation("animation.test.spline");
        BoneSnapshot body = AnimationProcessor.evaluate(animation, 0.5, MolangContext.EMPTY)
            .get("body");

        assertEquals(2, animation.length(), EPSILON);
        assertEquals(0.5625, body.getTranslateX(), EPSILON);
    }

    @Test
    public void interpolatesPreAndPostKeyframes() throws Exception {
        Animation animation = loadFixture().getAnimation("animation.test.prepost");
        BoneSnapshot body = AnimationProcessor.evaluate(animation, 0.5, MolangContext.EMPTY)
            .get("body");

        assertEquals(1, body.getTranslateX(), EPSILON);
    }

    private BakedAnimations loadFixture() throws Exception {
        try (Reader reader = new InputStreamReader(
            getClass().getResourceAsStream("/animations/headless.animation.json"),
            StandardCharsets.UTF_8)) {
            return new GeckoLibGsonLoader().loadAnimations(reader);
        }
    }
}
