package com.geckolib.renderer.base;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Immutable bone and locator transform collection for one posed model. */
public final class GeoRenderTransforms {

    private final Map<String, GeoRenderTransform> bones;
    private final Map<String, GeoRenderTransform> locators;

    GeoRenderTransforms(Map<String, GeoRenderTransform> bones, Map<String, GeoRenderTransform> locators) {
        this.bones = Collections.unmodifiableMap(new LinkedHashMap<>(bones));
        this.locators = Collections.unmodifiableMap(new LinkedHashMap<>(locators));
    }

    public Optional<GeoRenderTransform> getBone(String name) {
        return Optional.ofNullable(this.bones.get(name));
    }

    public Optional<GeoRenderTransform> getLocator(String name) {
        return Optional.ofNullable(this.locators.get(name));
    }

    public Map<String, GeoRenderTransform> bones() {
        return this.bones;
    }

    public Map<String, GeoRenderTransform> locators() {
        return this.locators;
    }
}
