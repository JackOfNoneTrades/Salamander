package com.geckolib.cache.model;

/** Immutable model-space vertex and texture coordinate. */
public final class GeoVertex {

    private final float posX;
    private final float posY;
    private final float posZ;
    private final float texU;
    private final float texV;

    public GeoVertex(double x, double y, double z) {
        this((float) x, (float) y, (float) z, 0, 0);
    }

    public GeoVertex(float posX, float posY, float posZ, float texU, float texV) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.texU = texU;
        this.texV = texV;
    }

    public float posX() {
        return this.posX;
    }

    public float posY() {
        return this.posY;
    }

    public float posZ() {
        return this.posZ;
    }

    public float texU() {
        return this.texU;
    }

    public float texV() {
        return this.texV;
    }

    public GeoVertex withUVs(double texU, double texV) {
        if (Double.compare(texU, this.texU) == 0 && Double.compare(texV, this.texV) == 0) return this;

        return new GeoVertex(this.posX, this.posY, this.posZ, (float) texU, (float) texV);
    }
}
