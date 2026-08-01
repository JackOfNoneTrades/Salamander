package com.geckolib.cache.animation.keyframeevent;

import java.util.Objects;

/** Particle instruction marker. */
public final class ParticleKeyframeData extends KeyFrameData {

    private final String effect;

    public ParticleKeyframeData(double time, String effect, String locator) {
        super(time, locator);
        this.effect = effect;
    }

    public String getEffect() {
        return this.effect;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), this.effect, getLocatorName());
    }
}
