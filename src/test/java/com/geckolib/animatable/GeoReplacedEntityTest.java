package com.geckolib.animatable;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;

public class GeoReplacedEntityTest {

    @Test
    public void sharedAnimatableKeepsManagersSeparateByEntityId() {
        TestReplacedEntity animatable = new TestReplacedEntity();
        AnimatableManager<GeoReplacedEntity> first = animatable.getAnimatableInstanceCache()
            .getManagerForId(4);
        AnimatableManager<GeoReplacedEntity> firstAgain = animatable.getAnimatableInstanceCache()
            .getManagerForId(4);
        AnimatableManager<GeoReplacedEntity> second = animatable.getAnimatableInstanceCache()
            .getManagerForId(5);

        assertSame(first, firstAgain);
        assertNotSame(first, second);
    }

    private static final class TestReplacedEntity implements GeoReplacedEntity {

        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
    }
}
