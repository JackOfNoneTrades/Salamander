package com.geckolib.animatable.instance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;

/** ID-keyed manager cache for shared singleton animatables such as items. */
public final class SingletonAnimatableInstanceCache extends AnimatableInstanceCache {

    private final Map<Long, AnimatableManager<?>> managers = new ConcurrentHashMap<>();

    public SingletonAnimatableInstanceCache(GeoAnimatable animatable) {
        super(animatable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends GeoAnimatable> AnimatableManager<T> getManagerForId(long uniqueId) {
        return (AnimatableManager<T>) this.managers
            .computeIfAbsent(uniqueId, ignored -> new AnimatableManager<>(this.animatable));
    }
}
