package com.geckolib.cache.animation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Immutable animation-name lookup for one animation resource. */
public final class BakedAnimations {

    private final Map<String, Animation> animations;

    public BakedAnimations(Map<String, Animation> animations) {
        this.animations = Collections.unmodifiableMap(new LinkedHashMap<>(animations));
    }

    public Map<String, Animation> animations() {
        return this.animations;
    }

    public Animation getAnimation(String name) {
        return this.animations.get(name);
    }
}
