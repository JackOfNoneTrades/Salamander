package com.geckolib.cache.model;

/** Non-rendering named transform relative to a model bone. */
public final class GeoLocator {

    private final GeoBone parent;
    private final String name;
    private final float offsetX;
    private final float offsetY;
    private final float offsetZ;
    private final float rotX;
    private final float rotY;
    private final float rotZ;

    public GeoLocator(GeoBone parent, String name, float offsetX, float offsetY, float offsetZ, float rotX, float rotY,
        float rotZ) {
        this.parent = parent;
        this.name = name;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
    }

    public GeoBone parent() {
        return this.parent;
    }

    public String name() {
        return this.name;
    }

    public float offsetX() {
        return this.offsetX;
    }

    public float offsetY() {
        return this.offsetY;
    }

    public float offsetZ() {
        return this.offsetZ;
    }

    public float rotX() {
        return this.rotX;
    }

    public float rotY() {
        return this.rotY;
    }

    public float rotZ() {
        return this.rotZ;
    }
}
