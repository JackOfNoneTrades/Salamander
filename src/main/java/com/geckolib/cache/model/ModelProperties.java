package com.geckolib.cache.model;

import net.minecraft.util.ResourceLocation;

/** Additional model metadata from the geometry description. */
public final class ModelProperties {

    private final ResourceLocation resourcePath;
    private final String identifier;
    private final Float visibleBoundsWidth;
    private final Float visibleBoundsHeight;
    private final GeoVector visibleBoundsOffset;
    private final int textureWidth;
    private final int textureHeight;

    public ModelProperties(ResourceLocation resourcePath, String identifier, Float visibleBoundsWidth,
        Float visibleBoundsHeight, GeoVector visibleBoundsOffset, int textureWidth, int textureHeight) {
        this.resourcePath = resourcePath;
        this.identifier = identifier;
        this.visibleBoundsWidth = visibleBoundsWidth;
        this.visibleBoundsHeight = visibleBoundsHeight;
        this.visibleBoundsOffset = visibleBoundsOffset;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    public ResourceLocation resourcePath() {
        return this.resourcePath;
    }

    public String identifier() {
        return this.identifier;
    }

    public Float visibleBoundsWidth() {
        return this.visibleBoundsWidth;
    }

    public Float visibleBoundsHeight() {
        return this.visibleBoundsHeight;
    }

    public GeoVector visibleBoundsOffset() {
        return this.visibleBoundsOffset;
    }

    public int textureWidth() {
        return this.textureWidth;
    }

    public int textureHeight() {
        return this.textureHeight;
    }
}
