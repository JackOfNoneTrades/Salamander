package com.geckolib.animatable;

import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;

/** Root interface for objects with GeckoLib animation controllers. */
public interface GeoAnimatable {

    void registerControllers(AnimatableManager.ControllerRegistrar controllers);

    AnimatableInstanceCache getAnimatableInstanceCache();

    default AnimatableInstanceCache animatableCacheOverride() {
        return null;
    }
}
