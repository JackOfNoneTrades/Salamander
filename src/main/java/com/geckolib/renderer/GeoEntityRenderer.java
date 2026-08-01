package com.geckolib.renderer;

import java.nio.FloatBuffer;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.constant.DataTickets;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.loading.math.value.Variable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.renderer.layer.GeoRenderLayersContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Vanilla-lifecycle entity renderer for living GeckoLib animatables. */
@SideOnly(Side.CLIENT)
public class GeoEntityRenderer<T extends EntityLivingBase & GeoAnimatable> extends RendererLivingEntity
    implements GeoRenderer<T> {

    private static final float VANILLA_MODEL_OFFSET = 24 / 16f + 0.0078125f;

    protected final GeoModel<T> model;
    protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);
    protected float scaleWidth = 1;
    protected float scaleHeight = 1;

    private final FloatBuffer currentColor = BufferUtils.createFloatBuffer(4);
    private T activeEntity;
    private ModelPose activePose;
    private float activePartialTicks;
    private float limbSwing;
    private float limbSwingAmount;
    private float ageInTicks;
    private float netHeadYaw;
    private float headPitch;

    public GeoEntityRenderer(GeoModel<T> model) {
        this(model, 0.5f);
    }

    public GeoEntityRenderer(GeoModel<T> model, float shadowRadius) {
        this(new DelegatingModel(), model, shadowRadius);
    }

    private GeoEntityRenderer(DelegatingModel dummyModel, GeoModel<T> model, float shadowRadius) {
        super(dummyModel, shadowRadius);
        this.model = model;
        dummyModel.renderer = this;
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return this.model;
    }

    @Override
    public List<GeoRenderLayer<T>> getRenderLayers() {
        return this.renderLayers.getRenderLayers();
    }

    public GeoEntityRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    public boolean removeRenderLayer(GeoRenderLayer<T> renderLayer) {
        return this.renderLayers.removeLayer(renderLayer);
    }

    public GeoEntityRenderer<T> withScale(float scale) {
        return withScale(scale, scale);
    }

    public GeoEntityRenderer<T> withScale(float widthScale, float heightScale) {
        this.scaleWidth = widthScale;
        this.scaleHeight = heightScale;

        return this;
    }

    /** Unique animation id for the rendered entity. */
    public long getInstanceId(T animatable) {
        return animatable.getEntityId();
    }

    /** ARGB model tint. */
    public int getRenderColor(T animatable, float partialTicks) {
        return 0xFFFFFFFF;
    }

    /** Movement threshold used by {@link com.geckolib.animation.state.AnimationTest#isMoving()}. */
    public float getMotionAnimThreshold(T animatable) {
        return 0.015f;
    }

    @Override
    public ResourceLocation getEntityTexture(Entity entity) {
        return this.model.getTextureResource(cast(entity));
    }

    @Override
    protected final void preRenderCallback(EntityLivingBase entity, float partialTicks) {
        scaleModelForRender(cast(entity), partialTicks, this.scaleWidth, this.scaleHeight);
    }

    /**
     * Counteracts vanilla ModelBase's inverted axes while applying the renderer scale.
     * Override and call super to add model-wide render transforms.
     */
    protected void scaleModelForRender(T animatable, float partialTicks, float widthScale, float heightScale) {
        GL11.glScalef(-widthScale, -heightScale, widthScale);
    }

    @Override
    protected void renderModel(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks,
        float netHeadYaw, float headPitch, float scale) {
        T animatable = cast(entity);

        this.activeEntity = animatable;
        this.activePose = null;
        this.activePartialTicks = ageInTicks - entity.ticksExisted;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;

        if (entity.isInvisible() && entity.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer)) return;

        int color = getRenderColor(animatable, this.activePartialTicks);
        float red = channel(color, 16);
        float green = channel(color, 8);
        float blue = channel(color, 0);
        float alpha = channel(color, 24);
        boolean translucentInvisible = entity.isInvisible();

        if (translucentInvisible) alpha *= 0.15f;

        GL11.glPushMatrix();

        try {
            // RendererLivingEntity translates ModelBase models down by 24 pixels. GeckoLib models use a ground origin.
            GL11.glTranslatef(0, VANILLA_MODEL_OFFSET + 0.01f, 0);
            renderGeoModel(animatable, red, green, blue, alpha, translucentInvisible || alpha < 1);
        } finally {
            GL11.glPopMatrix();
        }
    }

    protected void renderGeoModel(T animatable, float red, float green, float blue, float alpha, boolean translucent) {
        if (!translucent) {
            GeoRenderer.super.render(
                animatable,
                getInstanceId(animatable),
                this.ageInTicks,
                this.activePartialTicks,
                createMolangContext(animatable, this.activePartialTicks),
                red,
                green,
                blue,
                alpha);
            return;
        }

        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int blendSource = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        int blendDestination = GL11.glGetInteger(GL11.GL_BLEND_DST);
        int alphaFunction = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
        float alphaReference = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);

        try {
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glAlphaFunc(GL11.GL_GREATER, 1 / 255f);
            GeoRenderer.super.render(
                animatable,
                getInstanceId(animatable),
                this.ageInTicks,
                this.activePartialTicks,
                createMolangContext(animatable, this.activePartialTicks),
                red,
                green,
                blue,
                alpha);
        } finally {
            GL11.glDepthMask(depthMask);
            GL11.glBlendFunc(blendSource, blendDestination);
            GeoRenderer.setEnabled(GL11.GL_BLEND, blendEnabled);
            GL11.glAlphaFunc(alphaFunction, alphaReference);
        }
    }

    @Override
    public ModelPose createModelPose(T animatable, long instanceId, double animatableAge, MolangContext molangContext) {
        AnimatableManager<T> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(instanceId);
        boolean sitting = animatable.isRiding() && animatable.ridingEntity != null
            && animatable.ridingEntity.shouldRiderSit();

        manager.setAnimatableData(
            DataTickets.IS_MOVING,
            Math.abs(this.limbSwingAmount) > getMotionAnimThreshold(animatable));
        manager.setAnimatableData(DataTickets.LIMB_SWING, this.limbSwing);
        manager.setAnimatableData(DataTickets.LIMB_SWING_AMOUNT, this.limbSwingAmount);
        manager.setAnimatableData(DataTickets.NET_HEAD_YAW, this.netHeadYaw);
        manager.setAnimatableData(DataTickets.HEAD_PITCH, this.headPitch);
        manager.setAnimatableData(DataTickets.IS_CHILD, animatable.isChild());
        manager.setAnimatableData(DataTickets.IS_SITTING, sitting);
        manager.setAnimatableData(DataTickets.RENDER_COLOR, getRenderColor(animatable, this.activePartialTicks));

        ModelPose pose = GeoRenderer.super.createModelPose(animatable, instanceId, animatableAge, molangContext);

        this.activePose = pose;

        return pose;
    }

    /** Entity-aware model-pose hook, called after the model's own custom animation hook. */
    protected void applyRenderPose(T animatable, ModelPose pose, float limbSwing, float limbSwingAmount,
        float netHeadYaw, float headPitch, float partialTicks) {}

    @Override
    public void adjustModelPose(T animatable, ModelPose pose, float partialTicks) {
        applyRenderPose(
            animatable,
            pose,
            this.limbSwing,
            this.limbSwingAmount,
            this.netHeadYaw,
            this.headPitch,
            partialTicks);
    }

    /** Supplies common Bedrock entity queries. Unknown queries resolve to zero. */
    protected MolangContext createMolangContext(final T animatable, final float partialTicks) {
        return variableName -> resolveEntityQuery(animatable, partialTicks, variableName);
    }

    protected double resolveEntityQuery(T animatable, float partialTicks, String variableName) {
        String query = Variable.normalizeName(variableName);

        switch (query) {
            case "query.is_moving":
                return Math.abs(this.limbSwingAmount) > getMotionAnimThreshold(animatable) ? 1 : 0;
            case "query.is_on_ground":
                return animatable.onGround ? 1 : 0;
            case "query.is_in_water":
                return animatable.isInWater() ? 1 : 0;
            case "query.is_on_fire":
                return animatable.isBurning() ? 1 : 0;
            case "query.is_alive":
                return animatable.isEntityAlive() ? 1 : 0;
            case "query.health":
                return animatable.getHealth();
            case "query.max_health":
                return animatable.getMaxHealth();
            case "query.ground_speed":
            case "query.modified_move_speed":
                return Math.sqrt(animatable.motionX * animatable.motionX + animatable.motionZ * animatable.motionZ);
            case "query.vertical_speed":
                return animatable.motionY;
            case "query.yaw_speed":
                return MathHelper.wrapAngleTo180_double(animatable.rotationYaw - animatable.prevRotationYaw);
            case "query.life_time":
                return (animatable.ticksExisted + partialTicks) / 20d;
            case "query.time_of_day":
                return animatable.worldObj == null ? 0 : (animatable.worldObj.getTotalWorldTime() % 24000L) / 24000d;
            default:
                return 0;
        }
    }

    private void renderVanillaOverlay(Entity entity) {
        if (entity != this.activeEntity || this.activePose == null) return;

        this.currentColor.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, this.currentColor);
        renderModelGeometry(
            this.activePose,
            this.currentColor.get(0),
            this.currentColor.get(1),
            this.currentColor.get(2),
            this.currentColor.get(3));
    }

    @SuppressWarnings("unchecked")
    private T cast(Entity entity) {
        return (T) entity;
    }

    private static float channel(int color, int shift) {
        return (color >> shift & 255) / 255f;
    }

    private static final class DelegatingModel extends ModelBase {

        private GeoEntityRenderer<?> renderer;

        @Override
        public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
            if (this.renderer != null) this.renderer.renderVanillaOverlay(entity);
        }
    }
}
