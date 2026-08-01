package com.geckolib.cache.animation.keyframeevent;

import java.util.Objects;

/** Base data for an instruction marker in an animation timeline. */
public abstract class KeyFrameData {

    private final double animationTime;
    private final String locatorName;

    protected KeyFrameData(double animationTime, String locatorName) {
        this.animationTime = animationTime;
        this.locatorName = locatorName;
    }

    public double getTime() {
        return this.animationTime;
    }

    public String getLocatorName() {
        return this.locatorName;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass() && hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.animationTime);
    }
}
