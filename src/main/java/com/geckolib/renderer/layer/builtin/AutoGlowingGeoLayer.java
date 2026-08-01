package com.geckolib.renderer.layer.builtin;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Full-bright additive layer using GeckoLib 5's {@code _glowmask.png} texture convention. */
@SideOnly(Side.CLIENT)
public class AutoGlowingGeoLayer<T extends GeoAnimatable> extends TextureLayerGeoLayer<T> {

    public AutoGlowingGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    protected ResourceLocation getTextureResource(T animatable) {
        ResourceLocation baseTexture = getRenderer().getGeoModel()
            .getTextureResource(animatable);
        String path = baseTexture.getResourcePath();
        String glowPath = path.endsWith(".png") ? path.substring(0, path.length() - 4) + "_glowmask.png"
            : path + "_glowmask.png";

        return new ResourceLocation(baseTexture.getResourceDomain(), glowPath);
    }

    /** Override to retain the entity's world lighting instead of forcing full brightness. */
    protected boolean shouldRespectWorldLighting(T animatable) {
        return false;
    }

    @Override
    public void render(RenderPassInfo<T> renderPassInfo) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
        GL11.glDepthMask(false);

        if (!shouldRespectWorldLighting(renderPassInfo.animatable()))
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

        renderPassInfo.reRender(getTextureResource(renderPassInfo.animatable()), 1, 1, 1, 1);
    }
}
