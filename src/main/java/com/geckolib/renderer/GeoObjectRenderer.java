package com.geckolib.renderer;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.opengl.GL11;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.constant.DataTickets;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.GlStateSnapshot;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.renderer.layer.GeoRenderLayersContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Manual renderer for animatables that do not fit an entity, item, armor, or block-entity render lifecycle.
 *
 * <p>
 * Callers supply a packed light value and may associate an arbitrary transient object with each render. Override
 * {@link #getInstanceId(GeoAnimatable, Object)} when one animatable can represent multiple independently animated
 * objects.
 */
@SideOnly(Side.CLIENT)
public class GeoObjectRenderer<T extends GeoAnimatable, O> implements GeoRenderer<T> {

    protected final GeoModel<T> model;
    protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);
    protected float scaleWidth = 1;
    protected float scaleHeight = 1;

    private RenderContext<O> activeContext;

    public GeoObjectRenderer(GeoModel<T> model) {
        this.model = model;
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return this.model;
    }

    @Override
    public List<GeoRenderLayer<T>> getRenderLayers() {
        return this.renderLayers.getRenderLayers();
    }

    public GeoObjectRenderer<T, O> addRenderLayer(GeoRenderLayer<T> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    public boolean removeRenderLayer(GeoRenderLayer<T> renderLayer) {
        return this.renderLayers.removeLayer(renderLayer);
    }

    public GeoObjectRenderer<T, O> withScale(float scale) {
        return withScale(scale, scale);
    }

    public GeoObjectRenderer<T, O> withScale(float widthScale, float heightScale) {
        this.scaleWidth = widthScale;
        this.scaleHeight = heightScale;

        return this;
    }

    /** Animation identity for this render. Override when related objects represent independent instances. */
    public long getInstanceId(T animatable, O relatedObject) {
        return animatable.hashCode();
    }

    /** ARGB model tint. */
    public int getRenderColor(T animatable, O relatedObject, float partialTicks) {
        return 0xFFFFFFFF;
    }

    /**
     * Current animation time in ticks. World time is preferred so animations agree with other renderer types.
     */
    protected double getAnimatableAge(T animatable, O relatedObject, float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();

        return minecraft.theWorld == null ? Minecraft.getSystemTime() / 50d
            : minecraft.theWorld.getTotalWorldTime() + partialTicks;
    }

    /** Render using the identity and animation time supplied by this renderer's hooks. */
    public final void render(T animatable, O relatedObject, int packedLight, float partialTicks) {
        if (animatable == null) throw new IllegalArgumentException("Animatable cannot be null");

        render(
            animatable,
            relatedObject,
            getInstanceId(animatable, relatedObject),
            getAnimatableAge(animatable, relatedObject, partialTicks),
            packedLight,
            partialTicks);
    }

    /**
     * Render with a caller-defined identity and animation time. This is useful when an external synchronization layer
     * owns the instance ID.
     */
    public final void render(T animatable, O relatedObject, long instanceId, double animatableAge, int packedLight,
        float partialTicks) {
        if (animatable == null) throw new IllegalArgumentException("Animatable cannot be null");

        AnimatableManager<T> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(instanceId);
        int color = getRenderColor(animatable, relatedObject, partialTicks);
        RenderContext<O> previousContext = this.activeContext;
        manager.setAnimatableData(DataTickets.IS_MOVING, false);
        manager.setAnimatableData(DataTickets.RENDER_COLOR, color);
        manager.setAnimatableData(DataTickets.PACKED_LIGHT, packedLight);
        addRenderData(animatable, relatedObject, manager, partialTicks);
        GlStateSnapshot state = GlStateSnapshot.capture();
        boolean matrixPushed = false;

        this.activeContext = new RenderContext<>(relatedObject, instanceId);

        try {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            matrixPushed = true;
            OpenGlHelper.setLightmapTextureCoords(
                OpenGlHelper.lightmapTexUnit,
                packedLight & 0xFFFF,
                packedLight >>> 16 & 0xFFFF);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GeoRenderer.setEnabled(GL11.GL_CULL_FACE, shouldCullFaces(animatable, relatedObject));
            applyRenderPosition(animatable, relatedObject, partialTicks);
            GL11.glScalef(this.scaleWidth, this.scaleHeight, this.scaleWidth);
            applyRenderTransform(animatable, relatedObject, partialTicks);
            GeoRenderer.super.render(
                animatable,
                instanceId,
                animatableAge,
                partialTicks,
                createMolangContext(animatable, relatedObject, partialTicks),
                channel(color, 16),
                channel(color, 8),
                channel(color, 0),
                channel(color, 24));
        } finally {
            this.activeContext = previousContext;

            if (matrixPushed) {
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPopMatrix();
            }

            state.restore();
        }
    }

    /** Adds per-instance data without retaining the related object by default. */
    protected void addRenderData(T animatable, O relatedObject, AnimatableManager<T> manager, float partialTicks) {}

    /** Whether back faces should be culled for this render. */
    protected boolean shouldCullFaces(T animatable, O relatedObject) {
        return true;
    }

    /** Applies caller or world-space positioning before renderer scale. */
    protected void applyRenderPosition(T animatable, O relatedObject, float partialTicks) {}

    /** Applies object-space transforms after renderer scale has been applied. */
    protected void applyRenderTransform(T animatable, O relatedObject, float partialTicks) {
        GL11.glTranslatef(0.5f, 0.51f, 0.5f);
    }

    protected MolangContext createMolangContext(T animatable, O relatedObject, float partialTicks) {
        return MolangContext.EMPTY;
    }

    /** Related-object-aware model-pose hook. */
    protected void applyRenderPose(T animatable, O relatedObject, long instanceId, ModelPose pose,
        float partialTicks) {}

    /** Related-object-aware pre-render hook. */
    protected void preRender(T animatable, O relatedObject, long instanceId, ModelPose pose, float partialTicks) {}

    /** Related-object-aware post-render hook. */
    protected void postRender(T animatable, O relatedObject, long instanceId, ModelPose pose, float partialTicks) {}

    @Override
    public final void adjustModelPose(T animatable, ModelPose pose, float partialTicks) {
        RenderContext<O> context = this.activeContext;

        if (context != null) applyRenderPose(animatable, context.relatedObject, context.instanceId, pose, partialTicks);
    }

    @Override
    public final void preRender(T animatable, ModelPose pose, float partialTicks) {
        RenderContext<O> context = this.activeContext;

        if (context != null) preRender(animatable, context.relatedObject, context.instanceId, pose, partialTicks);
    }

    @Override
    public final void postRender(T animatable, ModelPose pose, float partialTicks) {
        RenderContext<O> context = this.activeContext;

        if (context != null) postRender(animatable, context.relatedObject, context.instanceId, pose, partialTicks);
    }

    private static float channel(int color, int shift) {
        return (color >> shift & 0xFF) / 255f;
    }

    private static final class RenderContext<O> {

        private final O relatedObject;
        private final long instanceId;

        private RenderContext(O relatedObject, long instanceId) {
            this.relatedObject = relatedObject;
            this.instanceId = instanceId;
        }
    }
}
