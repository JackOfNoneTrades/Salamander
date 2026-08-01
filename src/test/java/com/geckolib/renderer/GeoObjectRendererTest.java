package com.geckolib.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import net.minecraft.util.ResourceLocation;

import org.junit.Test;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.util.GeckoLibUtil;

public class GeoObjectRendererTest {

    @Test
    public void usesAnimatableIdentityByDefaultAndOwnsLayers() {
        TestAnimatable animatable = new TestAnimatable();
        TestRenderer renderer = new TestRenderer();
        GeoRenderLayer<TestAnimatable> layer = new TestLayer(renderer);

        assertEquals(animatable.hashCode(), renderer.getInstanceId(animatable, new RelatedObject()));
        assertSame(renderer, renderer.addRenderLayer(layer));
        assertSame(
            layer,
            renderer.getRenderLayers()
                .get(0));
        assertSame(renderer, renderer.withScale(0.5f));
        assertEquals(0.5f, renderer.scaleWidth(), 0);
        assertEquals(0.5f, renderer.scaleHeight(), 0);
        renderer.withScale(0.75f, 1.25f);
        assertEquals(0.75f, renderer.scaleWidth(), 0);
        assertEquals(1.25f, renderer.scaleHeight(), 0);
        assertFalse(
            renderer.getRenderLayers()
                .isEmpty());
        assertTrue(renderer.removeRenderLayer(layer));
    }

    private static final class RelatedObject {
    }

    private static final class TestLayer extends GeoRenderLayer<TestAnimatable> {

        private TestLayer(GeoRenderer<TestAnimatable> renderer) {
            super(renderer);
        }
    }

    private static final class TestRenderer extends GeoObjectRenderer<TestAnimatable, RelatedObject> {

        private TestRenderer() {
            super(new TestModel());
        }

        private float scaleWidth() {
            return this.scaleWidth;
        }

        private float scaleHeight() {
            return this.scaleHeight;
        }
    }

    private static final class TestAnimatable implements GeoAnimatable {

        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
    }

    private static final class TestModel extends GeoModel<TestAnimatable> {

        @Override
        public ResourceLocation getModelResource(TestAnimatable animatable) {
            return null;
        }

        @Override
        public ResourceLocation getTextureResource(TestAnimatable animatable) {
            return null;
        }

        @Override
        public ResourceLocation getAnimationResource(TestAnimatable animatable) {
            return null;
        }
    }
}
