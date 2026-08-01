package com.geckolib.renderer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;

import org.junit.Test;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.constant.ArmorRenderSlot;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoArmorRenderer.ArmorSegment;
import com.geckolib.util.GeckoLibUtil;

public class GeoArmorRendererTest {

    @Test
    public void usesGeckoLibFiveDefaultSegments() {
        GeoArmorRenderer<TestArmor> renderer = new GeoArmorRenderer<>(new TestArmorModel());

        assertEquals(Arrays.asList(ArmorSegment.HEAD), renderer.getSegmentsForSlot(ArmorRenderSlot.HEAD));
        assertEquals(
            Arrays.asList(ArmorSegment.CHEST, ArmorSegment.LEFT_ARM, ArmorSegment.RIGHT_ARM),
            renderer.getSegmentsForSlot(ArmorRenderSlot.CHEST));
        assertEquals(
            Arrays.asList(ArmorSegment.LEFT_LEG, ArmorSegment.RIGHT_LEG),
            renderer.getSegmentsForSlot(ArmorRenderSlot.LEGS));
        assertEquals(
            Arrays.asList(ArmorSegment.LEFT_FOOT, ArmorSegment.RIGHT_FOOT),
            renderer.getSegmentsForSlot(ArmorRenderSlot.FEET));
        assertEquals("armorLeftBoot", renderer.getBoneNameForSegment(ArmorSegment.LEFT_FOOT));
    }

    @Test
    public void registersByItemInstanceRatherThanClass() {
        TestArmor first = new TestArmor();
        TestArmor second = new TestArmor();
        GeoArmorRenderer<TestArmor> renderer = new GeoArmorRenderer<>(new TestArmorModel());

        GeoArmorRenderer.registerArmorRenderer(first, renderer);

        assertSame(renderer, GeoArmorRenderer.getArmorRenderer(first));
        assertNull(GeoArmorRenderer.getArmorRenderer(second));
    }

    private static final class TestArmor extends ItemArmor implements GeoItem {

        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        private TestArmor() {
            super(ArmorMaterial.IRON, 0, 0);
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
    }

    private static final class TestArmorModel extends GeoModel<TestArmor> {

        @Override
        public ResourceLocation getModelResource(TestArmor animatable) {
            return null;
        }

        @Override
        public ResourceLocation getTextureResource(TestArmor animatable) {
            return null;
        }

        @Override
        public ResourceLocation getAnimationResource(TestArmor animatable) {
            return null;
        }
    }
}
