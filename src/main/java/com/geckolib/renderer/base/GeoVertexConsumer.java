package com.geckolib.renderer.base;

/** Receives fully transformed GeckoLib vertices. */
@FunctionalInterface
public interface GeoVertexConsumer {

    void addVertex(float x, float y, float z, float textureU, float textureV, float normalX, float normalY,
        float normalZ, float red, float green, float blue, float alpha);
}
