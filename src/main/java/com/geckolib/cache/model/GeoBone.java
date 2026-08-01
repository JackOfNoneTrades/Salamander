package com.geckolib.cache.model;

/** Immutable baked bone hierarchy node. */
public abstract class GeoBone {

    protected final GeoBone parent;
    protected final String name;
    protected final GeoBone[] children;
    protected final GeoLocator[] locators;
    protected final float pivotX;
    protected final float pivotY;
    protected final float pivotZ;
    protected final float baseRotX;
    protected final float baseRotY;
    protected final float baseRotZ;

    protected GeoBone(GeoBone parent, String name, GeoBone[] children, GeoLocator[] locators, float pivotX,
        float pivotY, float pivotZ, float rotX, float rotY, float rotZ) {
        this.parent = parent;
        this.name = name;
        this.children = children;
        this.locators = locators;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.pivotZ = pivotZ;
        this.baseRotX = rotX;
        this.baseRotY = rotY;
        this.baseRotZ = rotZ;
    }

    public GeoBone parent() {
        return this.parent;
    }

    public String name() {
        return this.name;
    }

    public GeoBone[] children() {
        return this.children;
    }

    public GeoLocator[] locators() {
        return this.locators;
    }

    public float pivotX() {
        return this.pivotX;
    }

    public float pivotY() {
        return this.pivotY;
    }

    public float pivotZ() {
        return this.pivotZ;
    }

    public float baseRotX() {
        return this.baseRotX;
    }

    public float baseRotY() {
        return this.baseRotY;
    }

    public float baseRotZ() {
        return this.baseRotZ;
    }
}
