package com.geckolib.loading.loader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.Test;

import com.geckolib.cache.BakedModelCache;
import com.geckolib.cache.GeckoLibResources;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoLocator;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.google.gson.JsonParseException;

public class GeometryLoaderTest {

    private static final double EPSILON = 1.0E-6;
    private static final ResourceLocation MODEL_PATH = new ResourceLocation(
        "salamander",
        "geckolib/models/test.geo.json");

    @Test
    public void loadsHierarchyTransformsUvsAndLocators() throws Exception {
        BakedGeoModel model;

        try (InputStream stream = getClass().getResourceAsStream("/geckolib/models/test.geo.json")) {
            assertNotNull(stream);

            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                model = new GeckoLibGsonLoader().loadModel(MODEL_PATH, reader);
            }
        }

        assertEquals(
            "geometry.salamander_test",
            model.properties()
                .identifier());
        assertEquals(
            64,
            model.properties()
                .textureWidth());
        assertEquals(
            32,
            model.properties()
                .textureHeight());
        assertEquals(
            3.5,
            model.properties()
                .visibleBoundsWidth(),
            EPSILON);
        assertEquals(
            1.25,
            model.properties()
                .visibleBoundsOffset()
                .y(),
            EPSILON);
        assertEquals(1, model.topLevelBones().length);
        assertEquals(
            3,
            model.boneLookup()
                .size());

        GeoBone root = model.getBone("root")
            .get();
        GeoBone child = model.getBone("child")
            .get();
        GeoBone flat = model.getBone("flat")
            .get();

        assertSame(root, child.parent());
        assertSame(child, flat.parent());
        assertEquals(-1, root.pivotX(), EPSILON);
        assertEquals(2, root.pivotY(), EPSILON);
        assertEquals(3, root.pivotZ(), EPSILON);
        assertEquals(Math.toRadians(-10), root.baseRotX(), EPSILON);
        assertEquals(Math.toRadians(-20), root.baseRotY(), EPSILON);
        assertEquals(Math.toRadians(30), root.baseRotZ(), EPSILON);

        GeoCube rootCube = ((CuboidGeoBone) root).cubes()[0];

        assertEquals(
            8,
            rootCube.size()
                .x(),
            EPSILON);
        assertEquals(
            4,
            rootCube.pivot()
                .y(),
            EPSILON);
        assertEquals(
            Math.toRadians(-10),
            rootCube.rotation()
                .x(),
            EPSILON);

        for (GeoQuad quad : rootCube.quads()) {
            assertNotNull(quad);
        }

        GeoQuad mirroredWest = rootCube.quads()[0];

        assertEquals(ForgeDirection.WEST, mirroredWest.direction());
        assertEquals(1, mirroredWest.normalX(), EPSILON);
        assertEquals(0.3125, mirroredWest.vertices()[0].posX(), EPSILON);

        GeoQuad[] childQuads = ((CuboidGeoBone) child).cubes()[0].quads();

        assertNull(childQuads[0]);
        assertNull(childQuads[1]);
        assertNotNull(childQuads[2]);
        assertNull(childQuads[3]);
        assertNotNull(childQuads[4]);
        assertNull(childQuads[5]);

        GeoVertex northVertex = childQuads[2].vertices()[0];

        assertEquals(8d / 64d, northVertex.texU(), EPSILON);
        assertEquals(8d / 32d, northVertex.texV(), EPSILON);

        GeoQuad[] flatQuads = ((CuboidGeoBone) flat).cubes()[0].quads();

        assertNotNull(flatQuads[0]);
        assertNotNull(flatQuads[1]);
        assertNull(flatQuads[2]);
        assertNull(flatQuads[3]);
        assertNull(flatQuads[4]);
        assertNull(flatQuads[5]);

        GeoLocator hand = model.getLocator("hand")
            .get();
        GeoLocator look = model.getLocator("look")
            .get();

        assertSame(root, hand.parent());
        assertEquals(-2, hand.offsetX(), EPSILON);
        assertEquals(4, hand.offsetY(), EPSILON);
        assertEquals(6, hand.offsetZ(), EPSILON);
        assertEquals(Math.toRadians(-10), look.rotX(), EPSILON);
        assertEquals(Math.toRadians(-20), look.rotY(), EPSILON);
        assertEquals(Math.toRadians(30), look.rotZ(), EPSILON);
    }

    @Test
    public void rejectsInvalidBoneGraphs() {
        GeckoLibGsonLoader loader = new GeckoLibGsonLoader();
        String orphan = geometryWithBones("{\"name\":\"child\",\"parent\":\"missing\"}");
        String recursive = geometryWithBones(
            "{\"name\":\"first\",\"parent\":\"second\"}," + "{\"name\":\"second\",\"parent\":\"first\"}");

        assertThrows(JsonParseException.class, () -> loader.loadModel(MODEL_PATH, new java.io.StringReader(orphan)));
        assertThrows(JsonParseException.class, () -> loader.loadModel(MODEL_PATH, new java.io.StringReader(recursive)));
    }

    @Test
    public void normalizesResourcePathsAndUsesMissingModel() {
        ResourceLocation fullModel = new ResourceLocation("example", "geckolib/models/entity/test.geo.json");
        ResourceLocation fullAnimation = new ResourceLocation(
            "example",
            "geckolib/animations/entity/test.animation.json");

        assertEquals(new ResourceLocation("example", "entity/test"), GeckoLibResources.stripPrefixAndSuffix(fullModel));
        assertEquals(
            new ResourceLocation("example", "entity/test"),
            GeckoLibResources.stripPrefixAndSuffix(fullAnimation));
        assertEquals(
            new ResourceLocation("example", "entity/test"),
            GeckoLibResources.stripPrefixAndSuffix(new ResourceLocation("example", "models/entity/test.geo.json")));
        assertTrue(GeckoLibResources.findLoader(fullModel) instanceof GeckoLibGsonLoader);

        BakedModelCache empty = new BakedModelCache(Collections.emptyMap());

        assertSame(BakedModelCache.missingModel(), empty.getModel(new ResourceLocation("example", "absent")));
        assertTrue(
            BakedModelCache.missingModel()
                .isMissingno());
        assertFalse(new BakedGeoModel(new GeoBone[0], Collections.emptyMap(), null).isMissingno());
    }

    private static String geometryWithBones(String bones) {
        return "{\"format_version\":\"1.21.0\",\"minecraft:geometry\":[{\"description\":{"
            + "\"texture_width\":16,\"texture_height\":16},\"bones\":["
            + bones
            + "]}]}";
    }
}
