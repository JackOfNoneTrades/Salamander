package com.geckolib.renderer.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.geckolib.GeckoLibConstants;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.layer.GeoRenderLayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Base client renderer contract for immutable GeckoLib models.
 *
 * <p>
 * This is an interface so renderers can retain the appropriate vanilla renderer superclass and lifecycle.
 */
@SideOnly(Side.CLIENT)
public interface GeoRenderer<T extends GeoAnimatable> {

    GeoModel<T> getGeoModel();

    default List<GeoRenderLayer<T>> getRenderLayers() {
        return Collections.emptyList();
    }

    default ModelPose createModelPose(T animatable, long instanceId, double animatableAge,
        MolangContext molangContext) {
        GeoModel<T> geoModel = getGeoModel();
        BakedGeoModel model = geoModel.getBakedModel(animatable);
        BakedAnimations animations = geoModel.getBakedAnimations(animatable);
        AnimatableManager<T> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(instanceId);

        return AnimationProcessor.createModelPose(animatable, manager, animations, model, animatableAge, molangContext);
    }

    default void render(T animatable, long instanceId, double animatableAge, float partialTicks,
        MolangContext molangContext, float red, float green, float blue, float alpha) {
        AnimatableManager<T> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(instanceId);

        manager.setAnimatableData(com.geckolib.constant.DataTickets.ANIMATABLE_INSTANCE_ID, instanceId);
        manager.setAnimatableData(com.geckolib.constant.DataTickets.PARTIAL_TICK, partialTicks);
        manager.setAnimatableData(com.geckolib.constant.DataTickets.TICK, animatableAge);
        manager.setAnimatableData(com.geckolib.constant.DataTickets.ANIMATABLE_MANAGER, manager);

        ModelPose pose = createModelPose(animatable, instanceId, animatableAge, molangContext);

        getGeoModel().setCustomAnimations(animatable, instanceId, pose, partialTicks);
        adjustModelPose(animatable, pose, partialTicks);
        bindTexture(animatable);
        RenderPassInfo<T> renderPassInfo = new RenderPassInfo<>(
            this,
            animatable,
            pose.model(),
            pose,
            partialTicks,
            red,
            green,
            blue,
            alpha);

        renderModel(renderPassInfo, new ArrayList<>(getRenderLayers()));
    }

    /** Binds the primary model texture. Renderers embedded in a vanilla texture pass may override this. */
    default void bindTexture(T animatable) {
        Minecraft.getMinecraft().renderEngine.bindTexture(getGeoModel().getTextureResource(animatable));
    }

    default void adjustModelPose(T animatable, ModelPose pose, float partialTicks) {}

    default void preRender(T animatable, ModelPose pose, float partialTicks) {}

    default void postRender(T animatable, ModelPose pose, float partialTicks) {}

    default void renderModel(T animatable, ModelPose pose, float partialTicks, float red, float green, float blue,
        float alpha) {
        RenderPassInfo<T> renderPassInfo = new RenderPassInfo<>(
            this,
            animatable,
            pose.model(),
            pose,
            partialTicks,
            red,
            green,
            blue,
            alpha);

        renderModel(renderPassInfo, Collections.emptyList());
    }

    default void renderModel(RenderPassInfo<T> renderPassInfo, List<GeoRenderLayer<T>> renderLayers) {
        boolean textureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean rescaleNormalEnabled = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);

        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            preRender(renderPassInfo.animatable(), renderPassInfo.pose(), renderPassInfo.partialTick());

            if (!renderLayers.isEmpty()) {
                preApplyRenderLayers(renderPassInfo, renderLayers);
                renderPassInfo.captureTransforms();
            }

            renderModelGeometry(
                renderPassInfo.pose(),
                renderPassInfo.red(),
                renderPassInfo.green(),
                renderPassInfo.blue(),
                renderPassInfo.alpha());

            if (!renderLayers.isEmpty()) applyRenderLayers(renderPassInfo, renderLayers);
        } finally {
            try {
                postRender(renderPassInfo.animatable(), renderPassInfo.pose(), renderPassInfo.partialTick());
            } finally {
                setEnabled(GL11.GL_TEXTURE_2D, textureEnabled);
                setEnabled(GL12.GL_RESCALE_NORMAL, rescaleNormalEnabled);
                GL11.glColor4f(1, 1, 1, 1);
            }
        }
    }

    default void reRender(RenderPassInfo<T> renderPassInfo, ResourceLocation texture, float red, float green,
        float blue, float alpha) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        renderModelGeometry(renderPassInfo.pose(), red, green, blue, alpha);
    }

    default void preApplyRenderLayers(RenderPassInfo<T> renderPassInfo, List<GeoRenderLayer<T>> renderLayers) {
        for (GeoRenderLayer<T> renderLayer : renderLayers) {
            runRenderLayer(renderLayer, renderPassInfo, true);
        }
    }

    default void applyRenderLayers(RenderPassInfo<T> renderPassInfo, List<GeoRenderLayer<T>> renderLayers) {
        for (GeoRenderLayer<T> renderLayer : renderLayers) {
            runRenderLayer(renderLayer, renderPassInfo, false);
        }
    }

    static <T extends GeoAnimatable> void runRenderLayer(GeoRenderLayer<T> renderLayer,
        RenderPassInfo<T> renderPassInfo, boolean preRender) {
        GlStateSnapshot state = GlStateSnapshot.capture();

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();

        try {
            if (preRender) renderLayer.preRender(renderPassInfo);
            else renderLayer.render(renderPassInfo);
        } catch (RuntimeException exception) {
            GeckoLibConstants.LOGGER.error(
                "Exception in GeckoLib render layer {} for {}",
                renderLayer.getClass()
                    .getName(),
                renderPassInfo.animatable()
                    .getClass()
                    .getName(),
                exception);
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glPopMatrix();
            state.restore();
        }
    }

    /**
     * Emits an already-posed model without binding a texture or changing GL capabilities.
     *
     * <p>
     * Entity renderers use this for vanilla's hurt and color-multiplier passes.
     */
    default void renderModelGeometry(ModelPose pose, float red, float green, float blue, float alpha) {
        Tessellator tessellator = Tessellator.instance;
        boolean drawing = false;

        try {
            tessellator.startDrawingQuads();
            drawing = true;
            GeoModelRenderer
                .render(pose.model(), pose, new TessellatorVertexConsumer(tessellator), red, green, blue, alpha);
            drawing = false;
            tessellator.draw();
        } finally {
            if (drawing) tessellator.draw();
        }
    }

    static void setEnabled(int capability, boolean enabled) {
        if (enabled) GL11.glEnable(capability);
        else GL11.glDisable(capability);
    }

    final class TessellatorVertexConsumer implements GeoVertexConsumer {

        private final Tessellator tessellator;

        public TessellatorVertexConsumer(Tessellator tessellator) {
            this.tessellator = tessellator;
        }

        @Override
        public void addVertex(float x, float y, float z, float textureU, float textureV, float normalX, float normalY,
            float normalZ, float red, float green, float blue, float alpha) {
            this.tessellator.setColorRGBA_F(red, green, blue, alpha);
            this.tessellator.setNormal(normalX, normalY, normalZ);
            this.tessellator.addVertexWithUV(x, y, z, textureU, textureV);
        }
    }
}
