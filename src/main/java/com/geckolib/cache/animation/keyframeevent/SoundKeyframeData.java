package com.geckolib.cache.animation.keyframeevent;

import java.util.Objects;

/** Sound instruction marker. */
public final class SoundKeyframeData extends KeyFrameData {

    private final String sound;

    public SoundKeyframeData(double time, String sound, String locator) {
        super(time, locator);
        this.sound = sound;
    }

    public String getSound() {
        return this.sound;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), this.sound, getLocatorName());
    }
}
