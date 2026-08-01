package com.geckolib.renderer.base;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/** Immutable matrix snapshot for a bone or locator in one render pass. */
public final class GeoRenderTransform {

    private final Matrix4f modelSpaceMatrix;
    private final Matrix4f geometryMatrix;
    private final Matrix4f renderSpaceMatrix;

    GeoRenderTransform(Matrix4f modelSpaceMatrix, Matrix4f geometryMatrix, Matrix4f renderSpaceMatrix) {
        this.modelSpaceMatrix = new Matrix4f(modelSpaceMatrix);
        this.geometryMatrix = new Matrix4f(geometryMatrix);
        this.renderSpaceMatrix = new Matrix4f(renderSpaceMatrix);
    }

    /** Matrix positioned at the bone pivot or locator origin, relative to the model root. */
    public Matrix4f modelSpaceMatrix() {
        return new Matrix4f(this.modelSpaceMatrix);
    }

    /**
     * Matrix used to transform model geometry.
     *
     * <p>
     * For bones, this includes the translation away from the pivot. For locators it is the same as the model-space
     * matrix.
     */
    public Matrix4f geometryMatrix() {
        return new Matrix4f(this.geometryMatrix);
    }

    /** Camera-relative OpenGL render-space matrix captured for this render pass. */
    public Matrix4f renderSpaceMatrix() {
        return new Matrix4f(this.renderSpaceMatrix);
    }

    public Vector3f modelSpacePosition() {
        return position(this.modelSpaceMatrix);
    }

    public Vector3f renderSpacePosition() {
        return position(this.renderSpaceMatrix);
    }

    Matrix4f copyModelSpaceMatrix() {
        return new Matrix4f(this.modelSpaceMatrix);
    }

    private static Vector3f position(Matrix4f matrix) {
        Vector4f position = matrix.transform(new Vector4f(0, 0, 0, 1));

        return new Vector3f(position.x, position.y, position.z);
    }
}
