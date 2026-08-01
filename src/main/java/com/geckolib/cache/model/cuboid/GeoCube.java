package com.geckolib.cache.model.cuboid;

import com.geckolib.cache.model.GeoQuad;
import com.geckolib.cache.model.GeoVector;

/** Immutable baked cuboid and its local transform. */
public final class GeoCube {

    private final GeoQuad[] quads;
    private final GeoVector pivot;
    private final GeoVector rotation;
    private final GeoVector size;

    public GeoCube(GeoQuad[] quads, GeoVector pivot, GeoVector rotation, GeoVector size) {
        this.quads = quads;
        this.pivot = pivot;
        this.rotation = rotation;
        this.size = size;
    }

    public GeoQuad[] quads() {
        return this.quads;
    }

    public GeoVector pivot() {
        return this.pivot;
    }

    public GeoVector rotation() {
        return this.rotation;
    }

    public GeoVector size() {
        return this.size;
    }
}
