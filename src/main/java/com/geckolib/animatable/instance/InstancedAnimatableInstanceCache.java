package com.geckolib.animatable.instance;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;

/** Single-manager cache for entities and other naturally instanced animatables. */
public final class InstancedAnimatableInstanceCache extends AnimatableInstanceCache {

    private final AnimatableManager<?> manager;

    public InstancedAnimatableInstanceCache(GeoAnimatable animatable) {
        super(animatable);
        this.manager = new AnimatableManager<>(animatable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GeoAnimatable> AnimatableManager<T> getManagerForId(long uniqueId) {
        return (AnimatableManager<T>) this.manager;
    }
}
