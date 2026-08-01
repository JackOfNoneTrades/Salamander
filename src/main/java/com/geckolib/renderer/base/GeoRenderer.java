package com.geckolib.renderer.base;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationProcessor;
import com.geckolib.animation.state.ModelPose;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.math.MolangContext;
import com.geckolib.model.GeoModel;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Base client renderer for immutable GeckoLib models. */
@SideOnly(Side.CLIENT)
public abstract class GeoRenderer<T extends GeoAnimatable> {

    private final GeoModel<T> geoModel;

    protected GeoRenderer(GeoModel<T> geoModel) {
        this.geoModel = geoModel;
    }

    public GeoModel<T> getGeoModel() {
        return this.geoModel;
    }

    public ModelPose createModelPose(T animatable, long instanceId, double animatableAge, MolangContext molangContext) {
        BakedGeoModel model = this.geoModel.getBakedModel(animatable);
        BakedAnimations animations = this.geoModel.getBakedAnimations(animatable);
        AnimatableManager<T> manager = animatable.getAnimatableInstanceCache()
            .getManagerForId(instanceId);

        return AnimationProcessor.createModelPose(animatable, manager, animations, model, animatableAge, molangContext);
    }

    public void render(T animatable, long instanceId, double animatableAge, float partialTicks,
        MolangContext molangContext, float red, float green, float blue, float alpha) {
        ModelPose pose = createModelPose(animatable, instanceId, animatableAge, molangContext);

        adjustModelPose(animatable, pose, partialTicks);
        Minecraft.getMinecraft().renderEngine.bindTexture(this.geoModel.getTextureResource(animatable));
        renderModel(animatable, pose, partialTicks, red, green, blue, alpha);
    }

    protected void adjustModelPose(T animatable, ModelPose pose, float partialTicks) {}

    protected void preRender(T animatable, ModelPose pose, float partialTicks) {}

    protected void postRender(T animatable, ModelPose pose, float partialTicks) {}

    protected void renderModel(T animatable, ModelPose pose, float partialTicks, float red, float green, float blue,
        float alpha) {
        boolean textureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean rescaleNormalEnabled = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
        Tessellator tessellator = Tessellator.instance;
        boolean drawing = false;

        try {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            preRender(animatable, pose, partialTicks);
            tessellator.startDrawingQuads();
            drawing = true;
            GeoModelRenderer
                .render(pose.model(), pose, new TessellatorVertexConsumer(tessellator), red, green, blue, alpha);
            drawing = false;
            tessellator.draw();
            postRender(animatable, pose, partialTicks);
        } finally {
            if (drawing) tessellator.draw();

            setEnabled(GL11.GL_TEXTURE_2D, textureEnabled);
            setEnabled(GL12.GL_RESCALE_NORMAL, rescaleNormalEnabled);
            GL11.glColor4f(1, 1, 1, 1);
        }
    }

    private static void setEnabled(int capability, boolean enabled) {
        if (enabled) GL11.glEnable(capability);
        else GL11.glDisable(capability);
    }

    private static final class TessellatorVertexConsumer implements GeoVertexConsumer {

        private final Tessellator tessellator;

        private TessellatorVertexConsumer(Tessellator tessellator) {
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
