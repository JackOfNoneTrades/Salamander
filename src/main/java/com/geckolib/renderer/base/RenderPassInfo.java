package com.geckolib.renderer.base;

import java.nio.FloatBuffer;

import net.minecraft.util.ResourceLocation;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.model.BakedGeoModel;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Functionally immutable context for one immediate GeckoLib render pass. */
@SideOnly(Side.CLIENT)
public final class RenderPassInfo<T extends GeoAnimatable> {

    private static final ThreadLocal<FloatBuffer> MATRIX_BUFFER = ThreadLocal
        .withInitial(() -> BufferUtils.createFloatBuffer(16));

    private final GeoRenderer<T> renderer;
    private final T animatable;
    private final BakedGeoModel model;
    private final ModelPose pose;
    private final float partialTick;
    private final float red;
    private final float green;
    private final float blue;
    private final float alpha;
    private GeoRenderTransforms transforms;

    RenderPassInfo(GeoRenderer<T> renderer, T animatable, BakedGeoModel model, ModelPose pose, float partialTick,
        float red, float green, float blue, float alpha) {
        this.renderer = renderer;
        this.animatable = animatable;
        this.model = model;
        this.pose = pose;
        this.partialTick = partialTick;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public GeoRenderer<T> renderer() {
        return this.renderer;
    }

    public T animatable() {
        return this.animatable;
    }

    public BakedGeoModel model() {
        return this.model;
    }

    public ModelPose pose() {
        return this.pose;
    }

    public float partialTick() {
        return this.partialTick;
    }

    public float red() {
        return this.red;
    }

    public float green() {
        return this.green;
    }

    public float blue() {
        return this.blue;
    }

    public float alpha() {
        return this.alpha;
    }

    /** Available after all layer pre-render hooks have completed. */
    public GeoRenderTransforms transforms() {
        if (this.transforms == null)
            throw new IllegalStateException("Render transforms are unavailable during layer pre-render hooks");

        return this.transforms;
    }

    /** Re-renders the already-calculated pose without ticking animation controllers again. */
    public void reRender(ResourceLocation texture) {
        reRender(texture, this.red, this.green, this.blue, this.alpha);
    }

    /** Re-renders the already-calculated pose without ticking animation controllers again. */
    public void reRender(ResourceLocation texture, float red, float green, float blue, float alpha) {
        this.renderer.reRender(this, texture, red, green, blue, alpha);
    }

    /** Runs a render operation at the named bone's animated pivot. */
    public boolean runAtBone(String boneName, Runnable renderOperation) {
        GeoRenderTransform transform = transforms().getBone(boneName)
            .orElse(null);

        return runAtTransform(transform, renderOperation);
    }

    /** Runs a render operation at the named locator's animated origin. */
    public boolean runAtLocator(String locatorName, Runnable renderOperation) {
        GeoRenderTransform transform = transforms().getLocator(locatorName)
            .orElse(null);

        return runAtTransform(transform, renderOperation);
    }

    void captureTransforms() {
        if (this.transforms != null) throw new IllegalStateException("Render transforms have already been captured");

        FloatBuffer modelViewBuffer = MATRIX_BUFFER.get();

        modelViewBuffer.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewBuffer);
        this.transforms = GeoModelRenderer
            .captureTransforms(this.model, this.pose, new Matrix4f().set(modelViewBuffer));
    }

    private static boolean runAtTransform(GeoRenderTransform transform, Runnable renderOperation) {
        if (transform == null) return false;

        int matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        FloatBuffer matrixBuffer = MATRIX_BUFFER.get();

        matrixBuffer.clear();
        transform.copyModelSpaceMatrix()
            .get(matrixBuffer);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();

        try {
            GL11.glMultMatrix(matrixBuffer);
            renderOperation.run();
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            GL11.glMatrixMode(matrixMode);
        }

        return true;
    }
}
