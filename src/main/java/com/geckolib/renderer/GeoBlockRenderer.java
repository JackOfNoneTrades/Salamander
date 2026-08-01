package com.geckolib.renderer;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.cache.model.GeoVector;
import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.loading.math.value.Variable;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.geckolib.renderer.layer.GeoRenderLayersContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Minecraft 1.7 tile-entity renderer matching GeckoLib 5's block renderer surface. */
@SideOnly(Side.CLIENT)
public class GeoBlockRenderer<T extends TileEntity & GeoAnimatable> extends TileEntitySpecialRenderer
    implements GeoRenderer<T> {

    public static final DataTicket<ForgeDirection> DIRECTION_FACING = DataTicket
        .create("geoblockrenderer_direction_facing", ForgeDirection.class);

    private static final ForgeDirection[] BLOCK_DIRECTIONAL_FACINGS = { ForgeDirection.NORTH, ForgeDirection.EAST,
        ForgeDirection.SOUTH, ForgeDirection.WEST };

    protected final GeoModel<T> model;
    protected final GeoRenderLayersContainer<T> renderLayers = new GeoRenderLayersContainer<>(this);
    protected float scaleWidth = 1;
    protected float scaleHeight = 1;

    private double activeRenderX;
    private double activeRenderY;
    private double activeRenderZ;
    private ForgeDirection activeFacing = ForgeDirection.NORTH;
    private T activeAnimatable;
    private ModelPose activePose;
    private MaterialPass activeMaterialPass = MaterialPass.ALL;
    private boolean hasTranslucentBones;

    public GeoBlockRenderer(GeoModel<T> model) {
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

    public GeoBlockRenderer<T> addRenderLayer(GeoRenderLayer<T> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }

    public boolean removeRenderLayer(GeoRenderLayer<T> renderLayer) {
        return this.renderLayers.removeLayer(renderLayer);
    }

    public GeoBlockRenderer<T> withScale(float scale) {
        return withScale(scale, scale);
    }

    public GeoBlockRenderer<T> withScale(float widthScale, float heightScale) {
        this.scaleWidth = widthScale;
        this.scaleHeight = heightScale;

        return this;
    }

    /** Stable position-derived animation identity, matching GeckoLib 5's block renderer intent. */
    public long getInstanceId(T animatable) {
        return (animatable.yCoord + animatable.zCoord * 31L) * 31L + animatable.xCoord;
    }

    /** ARGB model tint. */
    public int getRenderColor(T animatable, float partialTicks) {
        return 0xFFFFFFFF;
    }

    /** Selects base-texture bones that need a separate blended pass. */
    protected boolean isBoneTranslucent(T animatable, GeoBone bone) {
        return false;
    }

    /** Whether back faces should be culled. Disable for explicitly double-sided model geometry. */
    protected boolean shouldCullFaces(T animatable) {
        return true;
    }

    /** Whether back faces should be culled during the blended bone pass. */
    protected boolean shouldCullTranslucentFaces(T animatable) {
        return shouldCullFaces(animatable);
    }

    /** Determine block facing from the 1.7 directional metadata convention. */
    protected ForgeDirection getBlockStateDirection(T animatable) {
        if (!animatable.hasWorldObj()) return ForgeDirection.NORTH;

        Block block = animatable.getBlockType();

        if (block instanceof BlockDirectional)
            return BLOCK_DIRECTIONAL_FACINGS[BlockDirectional.getDirection(animatable.getBlockMetadata())];

        return ForgeDirection.NORTH;
    }

    /** Rotate the model around its block center for the determined facing. */
    protected void rotateBlock(ForgeDirection facing) {
        switch (facing) {
            case SOUTH:
                GL11.glRotatef(180, 0, 1, 0);
                break;
            case WEST:
                GL11.glRotatef(90, 0, 1, 0);
                break;
            case EAST:
                GL11.glRotatef(-90, 0, 1, 0);
                break;
            case UP:
                GL11.glRotatef(90, 1, 0, 0);
                break;
            case DOWN:
                GL11.glRotatef(-90, 1, 0, 0);
                break;
            default:
                break;
        }
    }

    @Override
    public final void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
        if (!(tileEntity instanceof GeoAnimatable)) return;

        T animatable = cast(tileEntity);
        long instanceId = getInstanceId(animatable);
        double animatableAge = animatable.hasWorldObj() ? animatable.getWorldObj()
            .getTotalWorldTime() + partialTicks : partialTicks;
        int color = getRenderColor(animatable, partialTicks);
        float red = channel(color, 16);
        float green = channel(color, 8);
        float blue = channel(color, 0);
        float alpha = channel(color, 24);
        AnimatableManager<T> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(instanceId);

        this.activeAnimatable = animatable;
        this.activePose = null;
        this.activeMaterialPass = MaterialPass.ALL;
        this.hasTranslucentBones = false;
        this.activeRenderX = x;
        this.activeRenderY = y;
        this.activeRenderZ = z;
        this.activeFacing = getBlockStateDirection(animatable);
        manager.setAnimatableData(
            DataTickets.POSITION,
            new GeoVector(animatable.xCoord + 0.5, animatable.yCoord + 0.5, animatable.zCoord + 0.5));
        manager.setAnimatableData(DIRECTION_FACING, this.activeFacing);
        manager.setAnimatableData(DataTickets.IS_MOVING, false);
        manager.setAnimatableData(DataTickets.RENDER_COLOR, color);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();

        boolean cullingEnabled = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean alphaTestEnabled = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int blendSource = GL11.glGetInteger(GL11.GL_BLEND_SRC);
        int blendDestination = GL11.glGetInteger(GL11.GL_BLEND_DST);

        try {
            if (!shouldCullFaces(animatable)) GL11.glDisable(GL11.GL_CULL_FACE);

            applyRenderTransform(animatable, x, y, z, this.activeFacing, partialTicks);
            GL11.glScalef(this.scaleWidth, this.scaleHeight, this.scaleWidth);
            GeoRenderer.super.render(
                animatable,
                instanceId,
                animatableAge,
                partialTicks,
                createMolangContext(animatable, partialTicks),
                red,
                green,
                blue,
                alpha);

            if (this.hasTranslucentBones && this.activePose != null) {
                this.activeMaterialPass = MaterialPass.TRANSLUCENT;
                bindTexture(animatable);
                GeoRenderer.setEnabled(GL11.GL_CULL_FACE, shouldCullTranslucentFaces(animatable));
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glDisable(GL11.GL_ALPHA_TEST);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glDepthMask(false);
                renderModel(animatable, this.activePose, partialTicks, red, green, blue, alpha);
            }
        } finally {
            GL11.glDepthMask(depthMask);
            GL11.glBlendFunc(blendSource, blendDestination);
            GeoRenderer.setEnabled(GL11.GL_BLEND, blendEnabled);
            GeoRenderer.setEnabled(GL11.GL_ALPHA_TEST, alphaTestEnabled);
            GeoRenderer.setEnabled(GL11.GL_CULL_FACE, cullingEnabled);
            this.activeMaterialPass = MaterialPass.ALL;
            this.activeAnimatable = null;
            this.activePose = null;
            this.hasTranslucentBones = false;
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
        }
    }

    /** Applies block-world translation and facing before the model is posed and rendered. */
    protected void applyRenderTransform(T animatable, double x, double y, double z, ForgeDirection facing,
        float partialTicks) {
        GL11.glTranslated(x + 0.5, y + 0.01, z + 0.5);
        rotateBlock(facing);
    }

    /** Block-aware model-pose hook, called after the model's own custom animation hook. */
    protected void applyRenderPose(T animatable, ModelPose pose, double x, double y, double z, ForgeDirection facing,
        float partialTicks) {}

    @Override
    public ModelPose createModelPose(T animatable, long instanceId, double animatableAge, MolangContext molangContext) {
        ModelPose pose = GeoRenderer.super.createModelPose(animatable, instanceId, animatableAge, molangContext);

        this.activePose = pose;

        return pose;
    }

    @Override
    public final void adjustModelPose(T animatable, ModelPose pose, float partialTicks) {
        applyRenderPose(
            animatable,
            pose,
            this.activeRenderX,
            this.activeRenderY,
            this.activeRenderZ,
            this.activeFacing,
            partialTicks);

        this.activePose = pose;
        this.hasTranslucentBones = pose.model()
            .boneLookup()
            .values()
            .stream()
            .anyMatch(bone -> isBoneTranslucent(animatable, bone));

        if (this.hasTranslucentBones) {
            this.activeMaterialPass = MaterialPass.OPAQUE;
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthMask(true);
        }
    }

    @Override
    public void renderModelGeometry(ModelPose pose, float red, float green, float blue, float alpha) {
        switch (this.activeMaterialPass) {
            case OPAQUE:
                GeoRenderer.super.renderModelGeometry(
                    pose,
                    bone -> !isBoneTranslucent(this.activeAnimatable, bone),
                    red,
                    green,
                    blue,
                    alpha);
                break;
            case TRANSLUCENT:
                GeoRenderer.super.renderModelGeometry(
                    pose,
                    bone -> isBoneTranslucent(this.activeAnimatable, bone),
                    red,
                    green,
                    blue,
                    alpha);
                break;
            default:
                GeoRenderer.super.renderModelGeometry(pose, red, green, blue, alpha);
                break;
        }
    }

    /** Supplies common Bedrock block queries. Unknown queries resolve to zero. */
    protected MolangContext createMolangContext(final T animatable, final float partialTicks) {
        return variableName -> resolveBlockQuery(animatable, partialTicks, variableName);
    }

    protected double resolveBlockQuery(T animatable, float partialTicks, String variableName) {
        String query = Variable.normalizeName(variableName);

        switch (query) {
            case "query.life_time":
                return animatable.hasWorldObj() ? (animatable.getWorldObj()
                    .getTotalWorldTime() + partialTicks) / 20d : 0;
            case "query.time_of_day":
                return animatable.hasWorldObj() ? (animatable.getWorldObj()
                    .getTotalWorldTime() % 24000L) / 24000d : 0;
            default:
                return 0;
        }
    }

    private static float channel(int color, int shift) {
        return (color >> shift & 255) / 255f;
    }

    @SuppressWarnings("unchecked")
    private T cast(TileEntity tileEntity) {
        return (T) tileEntity;
    }

    private enum MaterialPass {

        ALL,
        OPAQUE,
        TRANSLUCENT
    }
}
