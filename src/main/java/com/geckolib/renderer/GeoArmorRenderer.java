package com.geckolib.renderer;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.constant.ArmorRenderSlot;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.loading.math.value.Variable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.GlStateSnapshot;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.renderer.layer.GeoRenderLayersContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Forge 1.7 humanoid armor adapter for singleton GeckoLib item animatables. */
@SideOnly(Side.CLIENT)
public class GeoArmorRenderer<T extends ItemArmor & GeoItem> extends ModelBiped implements GeoRenderer<T> {

    public static final DataTicket<ArmorRenderSlot> CURRENT_SLOT = DataTickets.ARMOR_SLOT;
    public static final DataTicket<Boolean> IS_GECKOLIB_WEARER = DataTicket
        .create("geoarmor_is_geckolib_wearer", Boolean.class);

    private static final float MODEL_OFFSET = 24 / 16f + 0.001f;
    private static final ResourceLocation ENCHANTED_ITEM_GLINT = new ResourceLocation(
        "textures/misc/enchanted_item_glint.png");
    private static final Map<ItemArmor, GeoArmorRenderer<?>> RENDERERS = Collections
        .synchronizedMap(new IdentityHashMap<ItemArmor, GeoArmorRenderer<?>>());
    private static final List<ArmorSegment> HEAD_SEGMENTS = Collections.singletonList(ArmorSegment.HEAD);
    private static final List<ArmorSegment> CHEST_SEGMENTS = Collections
        .unmodifiableList(Arrays.asList(ArmorSegment.CHEST, ArmorSegment.LEFT_ARM, ArmorSegment.RIGHT_ARM));
    private static final List<ArmorSegment> LEG_SEGMENTS = Collections
        .unmodifiableList(Arrays.asList(ArmorSegment.LEFT_LEG, ArmorSegment.RIGHT_LEG));
    private static final List<ArmorSegment> FOOT_SEGMENTS = Collections
        .unmodifiableList(Arrays.asList(ArmorSegment.LEFT_FOOT, ArmorSegment.RIGHT_FOOT));

    protected final GeoModel<T> model;
    protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);
    protected float scaleWidth = 1;
    protected float scaleHeight = 1;

    private FloatBuffer currentColor;
    private T currentArmorItem;
    private EntityLivingBase currentEntity;
    private ItemStack currentStack;
    private ArmorRenderSlot currentSlot;
    private boolean renderedSincePrepare;
    private ModelPose activePose;
    private T poseArmorItem;
    private EntityLivingBase poseEntity;
    private ArmorRenderSlot poseSlot;
    private long poseInstanceId;
    private float limbSwing;
    private float limbSwingAmount;
    private float ageInTicks;
    private float netHeadYaw;
    private float headPitch;
    private List<ArmorSegment> firstPersonSegments;
    private List<ArmorSegment> poseSegments;
    private Integer renderColorOverride;

    public GeoArmorRenderer(GeoModel<T> model) {
        super(1);
        this.model = model;
    }

    public static <I extends ItemArmor & GeoItem> void registerArmorRenderer(I armorItem,
        GeoArmorRenderer<I> renderer) {
        if (armorItem == null) throw new IllegalArgumentException("Armor item cannot be null");
        if (renderer == null) throw new IllegalArgumentException("Armor renderer cannot be null");

        RENDERERS.put(armorItem, renderer);
    }

    public static GeoArmorRenderer<?> getArmorRenderer(ItemArmor armorItem) {
        return RENDERERS.get(armorItem);
    }

    @Override
    public GeoModel<T> getGeoModel() {
        return this.model;
    }

    @Override
    public List<GeoRenderLayer<T>> getRenderLayers() {
        return this.renderLayers.getRenderLayers();
    }

    public GeoArmorRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    public boolean removeRenderLayer(GeoRenderLayer<T> renderLayer) {
        return this.renderLayers.removeLayer(renderLayer);
    }

    public GeoArmorRenderer<T> withScale(float scale) {
        return withScale(scale, scale);
    }

    public GeoArmorRenderer<T> withScale(float widthScale, float heightScale) {
        this.scaleWidth = widthScale;
        this.scaleHeight = heightScale;

        return this;
    }

    /** Segments rendered for one Forge armor pass. */
    public List<ArmorSegment> getSegmentsForSlot(ArmorRenderSlot slot) {
        switch (slot) {
            case HEAD:
                return HEAD_SEGMENTS;
            case CHEST:
                return CHEST_SEGMENTS;
            case LEGS:
                return LEG_SEGMENTS;
            case FEET:
                return FOOT_SEGMENTS;
            default:
                return Collections.emptyList();
        }
    }

    /** Standard GeckoLib 5 armor-template bone name for a humanoid segment. */
    public String getBoneNameForSegment(ArmorSegment segment) {
        return segment.defaultBoneName;
    }

    /** Unique armor animation identity, separate from ordinary item renders. */
    public long getInstanceId(ItemStack stack) {
        return GeoItem.getArmorId(stack);
    }

    /** ARGB model tint, multiplied by Forge's current armor-pass tint. */
    public int getRenderColor(T animatable, ItemStack stack, ArmorRenderSlot slot, float partialTicks) {
        return 0xFFFFFFFF;
    }

    /** Whether this chest-armor renderer should add geometry to vanilla's first-person arm pass. */
    public boolean shouldRenderFirstPersonArm(T animatable, ItemStack stack, EntityPlayer wearer) {
        return true;
    }

    /** Segments rendered for each vanilla first-person arm call. Minecraft 1.7 always supplies its right arm. */
    public List<ArmorSegment> getFirstPersonSegments(T animatable, ItemStack stack, EntityPlayer wearer) {
        return Collections.singletonList(ArmorSegment.RIGHT_ARM);
    }

    /** ARGB tint for the first-person pass, before any vanilla armor dye color is multiplied in. */
    public int getFirstPersonRenderColor(T animatable, ItemStack stack, EntityPlayer wearer, float partialTicks) {
        return getRenderColor(animatable, stack, ArmorRenderSlot.CHEST, partialTicks);
    }

    /** Additional first-person model-root transform applied before the standard armor-model transform. */
    protected void applyFirstPersonTransform(T animatable, ItemStack stack, EntityPlayer wearer, float partialTicks) {}

    /** First-person-specific pose hook, called after the ordinary armor pose hook. */
    protected void applyFirstPersonRenderPose(T animatable, ItemStack stack, EntityPlayer wearer, ModelPose pose,
        float partialTicks) {}

    /** Attempts to render registered GeckoLib chest armor over one vanilla first-person arm call. */
    public static boolean renderFirstPersonArm(EntityPlayer wearer, float partialTicks) {
        if (wearer == null) return false;

        ItemStack stack = wearer.inventory.armorItemInSlot(2);

        if (stack == null || !(stack.getItem() instanceof ItemArmor) || !(stack.getItem() instanceof GeoItem))
            return false;

        GeoArmorRenderer<?> renderer = getArmorRenderer((ItemArmor) stack.getItem());

        return renderer != null && renderer.renderFirstPersonArmUnchecked(wearer, stack, partialTicks);
    }

    /** Called by {@link com.geckolib.animatable.GeoArmorItem} before Forge renders an armor pass. */
    public final void prepareForRender(EntityLivingBase entity, ItemStack stack, int armorSlot) {
        if (entity == null) throw new IllegalArgumentException("Armor wearer cannot be null");
        if (stack == null || !(stack.getItem() instanceof ItemArmor) || !(stack.getItem() instanceof GeoItem))
            throw new IllegalArgumentException("Armor stack must contain an ItemArmor implementing GeoItem");

        this.currentEntity = entity;
        this.currentStack = stack;
        this.currentSlot = ArmorRenderSlot.fromArmorSlot(armorSlot);
        this.currentArmorItem = cast(stack.getItem());
        this.renderedSincePrepare = false;
        applyEntityState(entity);
    }

    /** Texture path returned to Forge before the vanilla armor pass begins. */
    public final net.minecraft.util.ResourceLocation getTextureResource(ItemStack stack) {
        return this.model.getTextureResource(cast(stack.getItem()));
    }

    @Override
    public final void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
        float headPitch, float scale) {
        if (this.currentArmorItem == null || this.currentEntity != entity
            || this.currentStack == null
            || this.currentSlot == null) return;

        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);

        float partialTicks = ageInTicks - entity.ticksExisted;
        boolean textureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();

        try {
            applyArmorRootTransform(true);

            if (!textureEnabled || this.renderedSincePrepare) {
                ensureCurrentPose(partialTicks);
                renderVanillaPass();
            } else {
                int color = getRenderColor(this.currentArmorItem, this.currentStack, this.currentSlot, partialTicks);
                float[] passColor = multiplyCurrentColor(color);

                GeoRenderer.super.render(
                    this.currentArmorItem,
                    getInstanceId(this.currentStack),
                    ageInTicks,
                    partialTicks,
                    createMolangContext(this.currentEntity, partialTicks),
                    passColor[0],
                    passColor[1],
                    passColor[2],
                    passColor[3]);
            }

            this.renderedSincePrepare = true;
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
        }
    }

    /** Forge has already bound the texture for base, dye-overlay, glint, and damage passes. */
    @Override
    public final void bindTexture(T animatable) {}

    @Override
    public ModelPose createModelPose(T animatable, long instanceId, double animatableAge, MolangContext context) {
        populateRenderData(animatable, instanceId);

        ModelPose pose = GeoRenderer.super.createModelPose(animatable, instanceId, animatableAge, context);

        rememberPose(pose, animatable, instanceId);

        return pose;
    }

    /** Armor-aware model-pose hook, called after the model's own custom animation hook. */
    protected void applyRenderPose(T animatable, ItemStack stack, EntityLivingBase wearer, ArmorRenderSlot slot,
        ModelPose pose, float partialTicks) {}

    @Override
    public final void adjustModelPose(T animatable, ModelPose pose, float partialTicks) {
        fitToBiped(pose);
        hideUnusedSegments(pose);
        applyRenderPose(animatable, this.currentStack, this.currentEntity, this.currentSlot, pose, partialTicks);

        if (isFirstPersonRender()) applyFirstPersonRenderPose(
            animatable,
            this.currentStack,
            (EntityPlayer) this.currentEntity,
            pose,
            partialTicks);
    }

    /** Supplies common Bedrock wearer queries. Unknown queries resolve to zero. */
    protected MolangContext createMolangContext(final EntityLivingBase wearer, final float partialTicks) {
        return variableName -> resolveEntityQuery(wearer, partialTicks, variableName);
    }

    protected double resolveEntityQuery(EntityLivingBase wearer, float partialTicks, String variableName) {
        String query = Variable.normalizeName(variableName);

        switch (query) {
            case "query.is_moving":
                return Math.abs(this.limbSwingAmount) > 0.015f ? 1 : 0;
            case "query.is_on_ground":
                return wearer.onGround ? 1 : 0;
            case "query.is_in_water":
                return wearer.isInWater() ? 1 : 0;
            case "query.is_on_fire":
                return wearer.isBurning() ? 1 : 0;
            case "query.is_alive":
                return wearer.isEntityAlive() ? 1 : 0;
            case "query.is_first_person":
                return isFirstPersonRender() ? 1 : 0;
            case "query.health":
                return wearer.getHealth();
            case "query.max_health":
                return wearer.getMaxHealth();
            case "query.ground_speed":
            case "query.modified_move_speed":
                return Math.sqrt(wearer.motionX * wearer.motionX + wearer.motionZ * wearer.motionZ);
            case "query.vertical_speed":
                return wearer.motionY;
            case "query.life_time":
                return (wearer.ticksExisted + partialTicks) / 20d;
            case "query.time_of_day":
                return wearer.worldObj == null ? 0 : (wearer.worldObj.getTotalWorldTime() % 24000L) / 24000d;
            default:
                return 0;
        }
    }

    private void ensureCurrentPose(float partialTicks) {
        long instanceId = getInstanceId(this.currentStack);

        if (isPoseCurrent(instanceId)) return;

        AnimatableManager<T> manager = this.currentArmorItem.getAnimatableInstanceCache()
            .getManagerForId(instanceId);

        manager.setAnimatableData(DataTickets.ANIMATABLE_INSTANCE_ID, instanceId);
        manager.setAnimatableData(DataTickets.PARTIAL_TICK, partialTicks);
        manager.setAnimatableData(DataTickets.TICK, (double) this.ageInTicks);
        manager.setAnimatableData(DataTickets.ANIMATABLE_MANAGER, manager);

        ModelPose pose = createModelPose(
            this.currentArmorItem,
            instanceId,
            this.ageInTicks,
            createMolangContext(this.currentEntity, partialTicks));

        this.model.setCustomAnimations(this.currentArmorItem, instanceId, pose, partialTicks);
        adjustModelPose(this.currentArmorItem, pose, partialTicks);
    }

    private void populateRenderData(T animatable, long instanceId) {
        AnimatableManager<T> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(instanceId);
        boolean sitting = this.currentEntity.isRiding() && this.currentEntity.ridingEntity != null
            && this.currentEntity.ridingEntity.shouldRiderSit();
        int color = this.renderColorOverride == null
            ? getRenderColor(
                animatable,
                this.currentStack,
                this.currentSlot,
                this.ageInTicks - this.currentEntity.ticksExisted)
            : this.renderColorOverride;

        manager.setAnimatableData(DataTickets.ENTITY, this.currentEntity);
        manager.setAnimatableData(DataTickets.ITEM_STACK, this.currentStack);
        manager.setAnimatableData(CURRENT_SLOT, this.currentSlot);
        manager.setAnimatableData(IS_GECKOLIB_WEARER, this.currentEntity instanceof GeoAnimatable);
        manager.setAnimatableData(DataTickets.IS_MOVING, Math.abs(this.limbSwingAmount) > 0.015f);
        manager.setAnimatableData(DataTickets.LIMB_SWING, this.limbSwing);
        manager.setAnimatableData(DataTickets.LIMB_SWING_AMOUNT, this.limbSwingAmount);
        manager.setAnimatableData(DataTickets.NET_HEAD_YAW, this.netHeadYaw);
        manager.setAnimatableData(DataTickets.HEAD_PITCH, this.headPitch);
        manager.setAnimatableData(DataTickets.IS_CHILD, this.currentEntity.isChild());
        manager.setAnimatableData(DataTickets.IS_SITTING, sitting);
        manager.setAnimatableData(DataTickets.IS_FIRST_PERSON, isFirstPersonRender());
        manager.setAnimatableData(
            DataTickets.PACKED_LIGHT,
            this.currentEntity.worldObj == null ? 0
                : this.currentEntity.worldObj.getLightBrightnessForSkyBlocks(
                    (int) Math.floor(this.currentEntity.posX),
                    (int) Math.floor(this.currentEntity.posY),
                    (int) Math.floor(this.currentEntity.posZ),
                    0));
        manager.setAnimatableData(DataTickets.RENDER_COLOR, color);
    }

    private void rememberPose(ModelPose pose, T animatable, long instanceId) {
        this.activePose = pose;
        this.poseArmorItem = animatable;
        this.poseEntity = this.currentEntity;
        this.poseSlot = this.currentSlot;
        this.poseInstanceId = instanceId;
        this.poseSegments = getCurrentSegments();
    }

    private boolean isPoseCurrent(long instanceId) {
        return this.activePose != null && this.poseArmorItem == this.currentArmorItem
            && this.poseEntity == this.currentEntity
            && this.poseSlot == this.currentSlot
            && this.poseInstanceId == instanceId
            && this.poseSegments.equals(getCurrentSegments());
    }

    private void renderVanillaPass() {
        float[] color = currentGlColor();

        renderModelGeometry(this.activePose, color[0], color[1], color[2], color[3]);
    }

    private void fitToBiped(ModelPose pose) {
        for (ArmorSegment segment : getCurrentSegments()) {
            BoneSnapshot snapshot = pose.get(getBoneNameForSegment(segment))
                .orElse(null);

            if (snapshot == null) continue;

            ModelRenderer part = getModelPart(segment);
            float translateX = part.rotationPointX;
            float translateY = -part.rotationPointY;

            switch (segment) {
                case LEFT_ARM:
                    translateX -= 5;
                    translateY += 2;
                    break;
                case RIGHT_ARM:
                    translateX += 5;
                    translateY += 2;
                    break;
                case LEFT_LEG:
                case LEFT_FOOT:
                    translateX -= 2;
                    translateY += 12;
                    break;
                case RIGHT_LEG:
                case RIGHT_FOOT:
                    translateX += 2;
                    translateY += 12;
                    break;
                default:
                    break;
            }

            snapshot.setRotation(-part.rotateAngleX, -part.rotateAngleY, part.rotateAngleZ)
                .setTranslation(translateX, translateY, part.rotationPointZ);
        }
    }

    private void hideUnusedSegments(ModelPose pose) {
        List<ArmorSegment> visibleSegments = getCurrentSegments();

        for (ArmorSegment segment : ArmorSegment.values()) {
            if (visibleSegments.contains(segment)) continue;

            pose.get(getBoneNameForSegment(segment))
                .ifPresent(
                    snapshot -> snapshot.skipRender(true)
                        .skipChildrenRender(true));
        }
    }

    private ModelRenderer getModelPart(ArmorSegment segment) {
        switch (segment) {
            case HEAD:
                return this.bipedHead;
            case CHEST:
                return this.bipedBody;
            case LEFT_ARM:
                return this.bipedLeftArm;
            case RIGHT_ARM:
                return this.bipedRightArm;
            case LEFT_LEG:
            case LEFT_FOOT:
                return this.bipedLeftLeg;
            case RIGHT_LEG:
            case RIGHT_FOOT:
                return this.bipedRightLeg;
            default:
                throw new IllegalArgumentException("Unknown armor segment: " + segment);
        }
    }

    private void applyEntityState(EntityLivingBase entity) {
        this.isSneak = entity.isSneaking();
        this.isRiding = entity.isRiding();
        this.isChild = entity.isChild();
        this.heldItemLeft = 0;
        this.heldItemRight = entity.getHeldItem() == null ? 0 : 1;
        this.aimedBow = false;

        if (!(entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) entity;
        ItemStack itemInUse = player.getItemInUse();

        if (itemInUse == null) return;

        EnumAction action = itemInUse.getItemUseAction();

        if (action == EnumAction.block) this.heldItemRight = 3;
        else if (action == EnumAction.bow) this.aimedBow = true;
    }

    private void applyChildTransform() {
        if (!this.isChild) return;

        if (this.currentSlot == ArmorRenderSlot.HEAD) {
            GL11.glScalef(0.75f, 0.75f, 0.75f);
            GL11.glTranslatef(0, 1, 0);
        } else {
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            GL11.glTranslatef(0, 1.5f, 0);
        }
    }

    private boolean renderFirstPersonArmUnchecked(EntityPlayer wearer, ItemStack stack, float partialTicks) {
        T animatable = cast(stack.getItem());

        if (!shouldRenderFirstPersonArm(animatable, stack, wearer)) return false;

        List<ArmorSegment> segments = getFirstPersonSegments(animatable, stack, wearer);

        if (segments == null) throw new IllegalStateException("First-person armor segments cannot be null");
        if (segments.isEmpty()) return false;

        prepareForRender(wearer, stack, ArmorRenderSlot.CHEST.armorSlot());
        int firstPersonColor = multiplyColors(
            getFirstPersonRenderColor(animatable, stack, wearer, partialTicks),
            ((ItemArmor) stack.getItem()).getColor(stack));

        this.firstPersonSegments = Collections.unmodifiableList(new ArrayList<>(segments));
        this.renderColorOverride = firstPersonColor;
        this.limbSwing = 0;
        this.limbSwingAmount = 0;
        this.ageInTicks = wearer.ticksExisted + partialTicks;
        this.netHeadYaw = 0;
        this.headPitch = 0;
        this.onGround = 0;
        this.isSneak = false;
        this.isRiding = false;
        this.isChild = false;
        this.heldItemLeft = 0;
        this.heldItemRight = 0;
        this.aimedBow = false;
        setRotationAngles(0, 0, 0, 0, 0, 1 / 16f, wearer);

        int packedLight = wearer.worldObj.getLightBrightnessForSkyBlocks(
            (int) Math.floor(wearer.posX),
            (int) Math.floor(wearer.posY),
            (int) Math.floor(wearer.posZ),
            0);
        int color = this.renderColorOverride;
        GlStateSnapshot state = GlStateSnapshot.capture();
        boolean matrixPushed = false;

        try {
            OpenGlHelper.setLightmapTextureCoords(
                OpenGlHelper.lightmapTexUnit,
                packedLight & 0xFFFF,
                packedLight >>> 16 & 0xFFFF);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            Minecraft.getMinecraft().renderEngine.bindTexture(this.model.getTextureResource(animatable));
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPushMatrix();
            matrixPushed = true;
            applyFirstPersonTransform(animatable, stack, wearer, partialTicks);
            applyArmorRootTransform(false);
            GeoRenderer.super.render(
                animatable,
                getInstanceId(stack),
                this.ageInTicks,
                partialTicks,
                createMolangContext(wearer, partialTicks),
                channel(color, 16),
                channel(color, 8),
                channel(color, 0),
                channel(color, 24));

            if (stack.isItemEnchanted()) renderFirstPersonGlint();
        } finally {
            this.firstPersonSegments = null;
            this.renderColorOverride = null;

            if (matrixPushed) {
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glPopMatrix();
            }

            state.restore();
        }

        return true;
    }

    private void applyArmorRootTransform(boolean applyChild) {
        if (applyChild) applyChildTransform();

        GL11.glTranslatef(0, MODEL_OFFSET, 0);
        GL11.glScalef(-this.scaleWidth, -this.scaleHeight, this.scaleWidth);
        GL11.glTranslatef(0, 0.01f, 0);
    }

    private void renderFirstPersonGlint() {
        Minecraft.getMinecraft().renderEngine.bindTexture(ENCHANTED_ITEM_GLINT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);

        for (int pass = 0; pass < 2; pass++) {
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glPushMatrix();

            try {
                GL11.glLoadIdentity();
                GL11.glScalef(1 / 3f, 1 / 3f, 1 / 3f);
                GL11.glRotatef(30 - pass * 60, 0, 0, 1);
                GL11.glTranslatef(0, this.ageInTicks * (0.001f + pass * 0.003f) * 20, 0);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                renderModelGeometry(this.activePose, 0.38f, 0.19f, 0.608f, 1);
            } finally {
                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glPopMatrix();
                GL11.glMatrixMode(GL11.GL_MODELVIEW);
            }
        }
    }

    private List<ArmorSegment> getCurrentSegments() {
        return this.firstPersonSegments == null ? getSegmentsForSlot(this.currentSlot) : this.firstPersonSegments;
    }

    private boolean isFirstPersonRender() {
        return this.firstPersonSegments != null;
    }

    private static int multiplyColors(int color, int armorColor) {
        if (armorColor < 0) return color;

        int red = (color >> 16 & 255) * (armorColor >> 16 & 255) / 255;
        int green = (color >> 8 & 255) * (armorColor >> 8 & 255) / 255;
        int blue = (color & 255) * (armorColor & 255) / 255;

        return color & 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private float[] multiplyCurrentColor(int color) {
        float[] current = currentGlColor();

        current[0] *= channel(color, 16);
        current[1] *= channel(color, 8);
        current[2] *= channel(color, 0);
        current[3] *= channel(color, 24);

        return current;
    }

    private float[] currentGlColor() {
        if (this.currentColor == null) this.currentColor = BufferUtils.createFloatBuffer(4);

        this.currentColor.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, this.currentColor);

        return new float[] { this.currentColor.get(0), this.currentColor.get(1), this.currentColor.get(2),
            this.currentColor.get(3) };
    }

    private static float channel(int color, int shift) {
        return (color >> shift & 255) / 255f;
    }

    @SuppressWarnings("unchecked")
    private T cast(Object armorItem) {
        return (T) armorItem;
    }

    /** Humanoid armor segments from GeckoLib 5's standard armor template. */
    public enum ArmorSegment {

        HEAD("armorHead"),
        CHEST("armorBody"),
        LEFT_ARM("armorLeftArm"),
        RIGHT_ARM("armorRightArm"),
        LEFT_LEG("armorLeftLeg"),
        RIGHT_LEG("armorRightLeg"),
        LEFT_FOOT("armorLeftBoot"),
        RIGHT_FOOT("armorRightBoot");

        private final String defaultBoneName;

        ArmorSegment(String defaultBoneName) {
            this.defaultBoneName = defaultBoneName;
        }

        public String defaultBoneName() {
            return this.defaultBoneName;
        }
    }
}
