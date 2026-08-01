package com.geckolib.cache.model.cuboid;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoLocator;

/** Baked bone containing cuboids. */
public final class CuboidGeoBone extends GeoBone {

    private final GeoCube[] cubes;

    public CuboidGeoBone(GeoBone parent, String name, GeoBone[] children, GeoCube[] cubes, GeoLocator[] locators,
        float pivotX, float pivotY, float pivotZ, float rotX, float rotY, float rotZ) {
        super(parent, name, children, locators, pivotX, pivotY, pivotZ, rotX, rotY, rotZ);
        this.cubes = cubes;
    }

    public GeoCube[] cubes() {
        return this.cubes;
    }
}
