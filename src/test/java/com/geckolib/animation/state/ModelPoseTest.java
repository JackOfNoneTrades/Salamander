package com.geckolib.animation.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.Collections;

import org.junit.Test;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;

public class ModelPoseTest {

    private static final double EPSILON = 1.0E-6;

    @Test
    public void isolatesModelInstancesAndComposesControllersInOrder() {
        BakedGeoModel model = createModel();
        ModelPose first = ModelPose.create(model);
        ModelPose second = ModelPose.create(model);
        BoneSnapshot firstBone = first.get("body")
            .get();
        BoneSnapshot secondBone = second.get("body")
            .get();

        assertNotSame(firstBone, secondBone);
        firstBone.setTranslation(2, 3, 4);
        assertEquals(0, secondBone.getTranslateX(), EPSILON);

        BoneSnapshot replacement = BoneSnapshot.create("body")
            .setScale(2, 3, 4)
            .setRotation(0.1f, 0.2f, 0.3f)
            .setTranslation(5, 6, 7);

        second.apply(Collections.singletonMap("body", replacement), false);
        assertEquals(5, secondBone.getTranslateX(), EPSILON);
        assertEquals(3, secondBone.getScaleY(), EPSILON);

        BoneSnapshot additive = BoneSnapshot.create("body")
            .setScale(0.5f, 2, 1)
            .setRotation(0.4f, 0.5f, 0.6f)
            .setTranslation(1, 2, 3);

        second.apply(Collections.singletonMap("body", additive), true);
        assertEquals(6, secondBone.getTranslateX(), EPSILON);
        assertEquals(6, secondBone.getScaleY(), EPSILON);
        assertEquals(0.5, secondBone.getRotX(), EPSILON);
    }

    private static BakedGeoModel createModel() {
        GeoBone body = new CuboidGeoBone(
            null,
            "body",
            new GeoBone[0],
            new GeoCube[0],
            new com.geckolib.cache.model.GeoLocator[0],
            0,
            0,
            0,
            0,
            0,
            0);

        return new BakedGeoModel(new GeoBone[] { body }, Collections.emptyMap(), null);
    }
}
