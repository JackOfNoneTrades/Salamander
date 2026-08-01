package com.geckolib.renderer.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.Test;

import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoLocator;
import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVector;
import com.geckolib.cache.model.GeoVertex;
import com.geckolib.cache.model.cuboid.CuboidGeoBone;
import com.geckolib.cache.model.cuboid.GeoCube;
import com.geckolib.loading.loader.GeckoLibGsonLoader;

public class GeoModelRendererTest {

    private static final double EPSILON = 1.0E-5;

    @Test
    public void appliesParentPoseToChildVerticesAndNormals() {
        BakedGeoModel model = createHierarchy();
        ModelPose pose = ModelPose.create(model);
        BoneSnapshot rootPose = pose.get("root")
            .get();
        List<Vertex> vertices = new ArrayList<>();

        rootPose.setTranslation(2, 0, 0);
        GeoModelRenderer.render(model, pose, collecting(vertices), 1, 0.5f, 0.25f, 1);

        assertEquals(4, vertices.size());
        assertEquals(-0.125, vertices.get(0).x, EPSILON);
        assertEquals(1, vertices.get(0).y, EPSILON);
        assertEquals(0, vertices.get(0).normalX, EPSILON);
        assertEquals(1, vertices.get(0).normalY, EPSILON);
        assertEquals(0.5, vertices.get(0).green, EPSILON);
    }

    @Test
    public void honorsBoneAndChildVisibilityWithoutMutatingModel() {
        BakedGeoModel model = createHierarchy();
        ModelPose pose = ModelPose.create(model);
        List<Vertex> vertices = new ArrayList<>();

        pose.get("child")
            .get()
            .skipRender(true);
        GeoModelRenderer.render(model, pose, collecting(vertices), 1, 1, 1, 1);
        assertTrue(vertices.isEmpty());

        ModelPose visiblePose = ModelPose.create(model);

        GeoModelRenderer.render(model, visiblePose, collecting(vertices), 1, 1, 1, 1);
        assertEquals(4, vertices.size());

        vertices.clear();
        visiblePose.get("root")
            .get()
            .skipChildrenRender(true);
        GeoModelRenderer.render(model, visiblePose, collecting(vertices), 1, 1, 1, 1);
        assertTrue(vertices.isEmpty());
    }

    @Test
    public void rendersParsedGeometryEndToEnd() throws Exception {
        BakedGeoModel model;

        try (Reader reader = new InputStreamReader(
            getClass().getResourceAsStream("/geckolib/models/test.geo.json"),
            StandardCharsets.UTF_8)) {
            model = new GeckoLibGsonLoader()
                .loadModel(new ResourceLocation("salamander", "geckolib/models/test.geo.json"), reader);
        }

        List<Vertex> vertices = new ArrayList<>();

        GeoModelRenderer.render(model, ModelPose.create(model), collecting(vertices), 1, 1, 1, 1);
        assertEquals(40, vertices.size());

        for (Vertex vertex : vertices) {
            assertTrue(Float.isFinite(vertex.x));
            assertTrue(Float.isFinite(vertex.y));
            assertTrue(Float.isFinite(vertex.normalX));
            assertTrue(Float.isFinite(vertex.normalY));
        }
    }

    private static BakedGeoModel createHierarchy() {
        GeoVertex[] points = new GeoVertex[] { new GeoVertex(1, 0, 0), new GeoVertex(1, 1, 0), new GeoVertex(1, 1, 1),
            new GeoVertex(1, 0, 1) };
        GeoQuad quad = new GeoQuad(points, 1, 0, 0, ForgeDirection.EAST);
        GeoCube cube = new GeoCube(new GeoQuad[] { quad }, GeoVector.ZERO, GeoVector.ZERO, new GeoVector(1, 1, 1));
        GeoBone[] children = new GeoBone[1];
        GeoBone root = new CuboidGeoBone(
            null,
            "root",
            children,
            new GeoCube[0],
            new GeoLocator[0],
            0,
            0,
            0,
            0,
            0,
            (float) (Math.PI / 2));
        GeoBone child = new CuboidGeoBone(
            root,
            "child",
            new GeoBone[0],
            new GeoCube[] { cube },
            new GeoLocator[0],
            0,
            0,
            0,
            0,
            0,
            0);

        children[0] = child;

        return new BakedGeoModel(new GeoBone[] { root }, Collections.emptyMap(), null);
    }

    private static GeoVertexConsumer collecting(List<Vertex> vertices) {
        return (x, y, z, textureU, textureV, normalX, normalY, normalZ, red, green, blue, alpha) -> vertices
            .add(new Vertex(x, y, z, normalX, normalY, green));
    }

    private static final class Vertex {

        private final float x;
        private final float y;
        private final float normalX;
        private final float normalY;
        private final float green;

        private Vertex(float x, float y, float z, float normalX, float normalY, float green) {
            this.x = x;
            this.y = y;
            this.normalX = normalX;
            this.normalY = normalY;
            this.green = green;
        }
    }
}
