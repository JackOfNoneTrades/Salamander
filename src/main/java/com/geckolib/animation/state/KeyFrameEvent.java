package com.geckolib.animation.state;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.cache.animation.keyframeevent.KeyFrameData;

/** Event emitted when an animation controller crosses an instruction marker. */
public final class KeyFrameEvent<T extends GeoAnimatable, E extends KeyFrameData> {

    private final T animatable;
    private final AnimatableManager<T> manager;
    private final AnimationController<T> controller;
    private final E keyframeData;
    private final float partialTick;

    public KeyFrameEvent(T animatable, AnimatableManager<T> manager, AnimationController<T> controller, E keyframeData,
        float partialTick) {
        this.animatable = animatable;
        this.manager = manager;
        this.controller = controller;
        this.keyframeData = keyframeData;
        this.partialTick = partialTick;
    }

    public T animatable() {
        return this.animatable;
    }

    /** Legacy render-context replacement for accessing per-instance data tickets. */
    public AnimatableManager<T> manager() {
        return this.manager;
    }

    public AnimationController<T> controller() {
        return this.controller;
    }

    public E keyframeData() {
        return this.keyframeData;
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    public double getAnimationTick() {
        return this.controller.getCurrentAnimationTime();
    }
}
