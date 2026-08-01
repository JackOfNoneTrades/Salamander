package com.geckolib.cache.model;

import net.minecraftforge.common.util.ForgeDirection;

/** Immutable baked quad data. Rendering is supplied by the client module. */
public final class GeoQuad {

    private final GeoVertex[] vertices;
    private final float normalX;
    private final float normalY;
    private final float normalZ;
    private final ForgeDirection direction;

    public GeoQuad(GeoVertex[] vertices, float normalX, float normalY, float normalZ, ForgeDirection direction) {
        this.vertices = vertices;
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
        this.direction = direction;
    }

    public GeoVertex[] vertices() {
        return this.vertices;
    }

    public float normalX() {
        return this.normalX;
    }

    public float normalY() {
        return this.normalY;
    }

    public float normalZ() {
        return this.normalZ;
    }

    public ForgeDirection direction() {
        return this.direction;
    }
}
