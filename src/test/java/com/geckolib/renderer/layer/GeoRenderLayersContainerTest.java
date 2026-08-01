package com.geckolib.renderer.layer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderer;

public class GeoRenderLayersContainerTest {

    @Test
    public void preservesInsertionOrderAndRejectsForeignLayers() {
        GeoRenderer<TestAnimatable> renderer = new TestRenderer();
        GeoRenderer<TestAnimatable> foreignRenderer = new TestRenderer();
        GeoRenderLayersContainer<TestAnimatable> layers = new GeoRenderLayersContainer<>(renderer);
        GeoRenderLayer<TestAnimatable> first = new TestLayer(renderer);
        GeoRenderLayer<TestAnimatable> second = new TestLayer(renderer);

        layers.addLayer(first);
        layers.addLayer(second);

        assertEquals(
            first,
            layers.getRenderLayers()
                .get(0));
        assertEquals(
            second,
            layers.getRenderLayers()
                .get(1));
        assertThrows(
            UnsupportedOperationException.class,
            () -> layers.getRenderLayers()
                .clear());
        assertThrows(IllegalArgumentException.class, () -> layers.addLayer(new TestLayer(foreignRenderer)));

        layers.removeLayer(first);
        assertEquals(
            second,
            layers.getRenderLayers()
                .get(0));
    }

    private static final class TestLayer extends GeoRenderLayer<TestAnimatable> {

        private TestLayer(GeoRenderer<TestAnimatable> renderer) {
            super(renderer);
        }
    }

    private static final class TestRenderer implements GeoRenderer<TestAnimatable> {

        @Override
        public GeoModel<TestAnimatable> getGeoModel() {
            return null;
        }
    }

    private static final class TestAnimatable implements GeoAnimatable {

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return null;
        }
    }
}
