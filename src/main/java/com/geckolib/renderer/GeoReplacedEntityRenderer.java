package com.geckolib.renderer;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.geckolib.animatable.GeoReplacedEntity;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.model.GeoVector;
import com.geckolib.constant.DataTickets;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.loading.math.value.Variable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.renderer.layer.GeoRenderLayersContainer;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Replaces an existing living entity renderer with one shared GeckoLib animatable. */
@SideOnly(Side.CLIENT)
public class GeoReplacedEntityRenderer<T extends GeoReplacedEntity, E extends EntityLivingBase>
    extends RendererLivingEntity implements GeoRenderer<T> {

    private static final float VANILLA_MODEL_OFFSET = 24 / 16f + 0.0078125f;
    private static final Map<Class<? extends Entity>, GeoReplacedEntityRenderer<?, ?>> REPLACED_RENDERERS = new ConcurrentHashMap<>();

    protected final GeoModel<T> model;
    protected final T animatable;
    protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);
    protected float scaleWidth = 1;
    protected float scaleHeight = 1;

    // LWJGL 2 requires 16 floats for glGetFloat, even when querying only RGBA.
    private final FloatBuffer currentColor = BufferUtils.createFloatBuffer(16);
    private E activeEntity;
    private ModelPose activePose;
    private float activePartialTicks;
    private float limbSwing;
    private float limbSwingAmount;
    private float ageInTicks;
    private float netHeadYaw;
    private float headPitch;

    public GeoReplacedEntityRenderer(GeoModel<T> model, T animatable) {
        this(model, animatable, 0.5f);
    }

    public GeoReplacedEntityRenderer(GeoModel<T> model, T animatable, float shadowRadius) {
        this(new DelegatingModel(), model, animatable, shadowRadius);
    }

    private GeoReplacedEntityRenderer(DelegatingModel dummyModel, GeoModel<T> model, T animatable, float shadowRadius) {
        super(dummyModel, shadowRadius);
        this.model = model;
        this.animatable = animatable;
        dummyModel.renderer = this;

        if (animatable instanceof Entity)
            throw new IllegalArgumentException("A replaced-entity animatable must not be an Entity instance");
    }

    /** Registers this renderer for an existing entity class. Call from the physical client only. */
    public static <T extends GeoReplacedEntity, E extends EntityLivingBase> void registerReplacedEntity(
        Class<E> entityClass, GeoReplacedEntityRenderer<T, E> renderer) {
        REPLACED_RENDERERS.put(entityClass, renderer);
        RenderingRegistry.registerEntityRenderingHandler(entityClass, renderer);
    }

    /** Returns the shared replacement animatable selected for an entity, including superclass registrations. */
    public static GeoReplacedEntity getReplacedAnimatable(Entity entity) {
        if (entity == null) return null;

        Class<?> entityClass = entity.getClass();

        while (Entity.class.isAssignableFrom(entityClass)) {
            GeoReplacedEntityRenderer<?, ?> renderer = REPLACED_RENDERERS.get(entityClass);

            if (renderer != null) return renderer.getAnimatable();

            entityClass = entityClass.getSuperclass();
        }

        return null;
    }

    public T getAnimatable() {
        return this.animatable;
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return this.model;
    }

    @Override
    public List<GeoRenderLayer<T>> getRenderLayers() {
        return this.renderLayers.getRenderLayers();
    }

    public GeoReplacedEntityRenderer<T, E> addRenderLayer(GeoRenderLayer<T> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    public boolean removeRenderLayer(GeoRenderLayer<T> renderLayer) {
        return this.renderLayers.removeLayer(renderLayer);
    }

    public GeoReplacedEntityRenderer<T, E> withScale(float scale) {
        return withScale(scale, scale);
    }

    public GeoReplacedEntityRenderer<T, E> withScale(float widthScale, float heightScale) {
        this.scaleWidth = widthScale;
        this.scaleHeight = heightScale;

        return this;
    }

    public long getInstanceId(T animatable, E replacedEntity) {
        return replacedEntity.getEntityId();
    }

    /** ARGB model tint. */
    public int getRenderColor(T animatable, E replacedEntity, float partialTicks) {
        return 0xFFFFFFFF;
    }

    /** Movement threshold used by {@link com.geckolib.animation.state.AnimationTest#isMoving()}. */
    public float getMotionAnimThreshold(T animatable) {
        return 0.015f;
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return this.model.getTextureResource(this.animatable);
    }

    @Override
    protected final void preRenderCallback(EntityLivingBase entity, float partialTicks) {
        scaleModelForRender(this.animatable, cast(entity), partialTicks, this.scaleWidth, this.scaleHeight);
    }

    /** Counteracts vanilla ModelBase's inverted axes while applying the renderer scale. */
    protected void scaleModelForRender(T animatable, E replacedEntity, float partialTicks, float widthScale,
        float heightScale) {
        GL11.glScalef(-widthScale, -heightScale, widthScale);
    }

    @Override
    protected void renderModel(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks,
        float netHeadYaw, float headPitch, float scale) {
        E replacedEntity = cast(entity);

        this.activeEntity = replacedEntity;
        this.activePose = null;
        this.activePartialTicks = ageInTicks - entity.ticksExisted;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;

        if (entity.isInvisible() && entity.isInvisibleToPlayer(Minecraft.getMinecraft().thePlayer)) return;

        int color = getRenderColor(this.animatable, replacedEntity, this.activePartialTicks);
        float red = channel(color, 16);
        float green = channel(color, 8);
        float blue = channel(color, 0);
        float alpha = channel(color, 24);
        boolean translucentInvisible = entity.isInvisible();

        if (translucentInvisible) alpha *= 0.15f;

        GL11.glPushMatrix();

        try {
            GL11.glTranslatef(0, VANILLA_MODEL_OFFSET + 0.01f, 0);
            renderGeoModel(red, green, blue, alpha, translucentInvisible || alpha < 1);
        } finally {
            GL11.glPopMatrix();
        }
    }

    protected void renderGeoModel(float red, float green, float blue, float alpha, boolean translucent) {
        long instanceId = getInstanceId(this.animatable, this.activeEntity);

        if (!translucent) {
            GeoRenderer.super.render(
                this.animatable,
                instanceId,
                this.ageInTicks,
                this.activePartialTicks,
                createMolangContext(this.activeEntity, this.activePartialTicks),
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
                this.animatable,
                instanceId,
                this.ageInTicks,
                this.activePartialTicks,
                createMolangContext(this.activeEntity, this.activePartialTicks),
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
        E entity = this.activeEntity;
        boolean sitting = entity.isRiding() && entity.ridingEntity != null && entity.ridingEntity.shouldRiderSit();

        manager.setAnimatableData(DataTickets.ENTITY, entity);
        manager.setAnimatableData(DataTickets.POSITION, new GeoVector(entity.posX, entity.posY, entity.posZ));
        manager.setAnimatableData(
            DataTickets.IS_MOVING,
            Math.abs(this.limbSwingAmount) > getMotionAnimThreshold(animatable));
        manager.setAnimatableData(DataTickets.LIMB_SWING, this.limbSwing);
        manager.setAnimatableData(DataTickets.LIMB_SWING_AMOUNT, this.limbSwingAmount);
        manager.setAnimatableData(DataTickets.NET_HEAD_YAW, this.netHeadYaw);
        manager.setAnimatableData(DataTickets.HEAD_PITCH, this.headPitch);
        manager.setAnimatableData(DataTickets.IS_CHILD, entity.isChild());
        manager.setAnimatableData(DataTickets.IS_SITTING, sitting);
        manager
            .setAnimatableData(DataTickets.RENDER_COLOR, getRenderColor(animatable, entity, this.activePartialTicks));

        ModelPose pose = GeoRenderer.super.createModelPose(animatable, instanceId, animatableAge, molangContext);

        this.activePose = pose;

        return pose;
    }

    /** Replaced-entity-aware model-pose hook, called after the model's custom animation hook. */
    protected void applyRenderPose(T animatable, E replacedEntity, ModelPose pose, float limbSwing,
        float limbSwingAmount, float netHeadYaw, float headPitch, float partialTicks) {}

    @Override
    public void adjustModelPose(T animatable, ModelPose pose, float partialTicks) {
        applyRenderPose(
            animatable,
            this.activeEntity,
            pose,
            this.limbSwing,
            this.limbSwingAmount,
            this.netHeadYaw,
            this.headPitch,
            partialTicks);
    }

    protected MolangContext createMolangContext(final E replacedEntity, final float partialTicks) {
        return variableName -> resolveEntityQuery(replacedEntity, partialTicks, variableName);
    }

    /** Supplies common Bedrock queries from the entity whose renderer is being replaced. */
    protected double resolveEntityQuery(E entity, float partialTicks, String variableName) {
        String query = Variable.normalizeName(variableName);

        switch (query) {
            case "query.is_moving":
                return Math.abs(this.limbSwingAmount) > getMotionAnimThreshold(this.animatable) ? 1 : 0;
            case "query.is_on_ground":
                return entity.onGround ? 1 : 0;
            case "query.is_in_water":
                return entity.isInWater() ? 1 : 0;
            case "query.is_on_fire":
                return entity.isBurning() ? 1 : 0;
            case "query.is_alive":
                return entity.isEntityAlive() ? 1 : 0;
            case "query.health":
                return entity.getHealth();
            case "query.max_health":
                return entity.getMaxHealth();
            case "query.ground_speed":
            case "query.modified_move_speed":
                return Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
            case "query.vertical_speed":
                return entity.motionY;
            case "query.yaw_speed":
                return MathHelper.wrapAngleTo180_double(entity.rotationYaw - entity.prevRotationYaw);
            case "query.life_time":
                return (entity.ticksExisted + partialTicks) / 20d;
            case "query.time_of_day":
                return entity.worldObj == null ? 0 : (entity.worldObj.getTotalWorldTime() % 24000L) / 24000d;
            default:
                return 0;
        }
    }

    private void renderVanillaOverlay(Entity entity) {
        if (entity != this.activeEntity || this.activePose == null) return;

        this.currentColor.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, this.currentColor);
        GL11.glPushMatrix();

        try {
            GL11.glTranslatef(0, VANILLA_MODEL_OFFSET + 0.01f, 0);
            renderModelGeometry(
                this.activePose,
                this.currentColor.get(0),
                this.currentColor.get(1),
                this.currentColor.get(2),
                this.currentColor.get(3));
        } finally {
            GL11.glPopMatrix();
        }
    }

    @SuppressWarnings("unchecked")
    private E cast(Entity entity) {
        return (E) entity;
    }

    private static float channel(int color, int shift) {
        return (color >> shift & 255) / 255f;
    }

    private static final class DelegatingModel extends ModelBase {

        private GeoReplacedEntityRenderer<?, ?> renderer;

        @Override
        public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
            float headPitch, float scale) {
            if (this.renderer != null) this.renderer.renderVanillaOverlay(entity);
        }
    }
}
