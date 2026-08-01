package com.geckolib.renderer.layer.builtin;

import net.minecraft.util.ResourceLocation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Re-renders the current pose once with a supplementary texture. */
@SideOnly(Side.CLIENT)
public class TextureLayerGeoLayer<T extends GeoAnimatable> extends GeoRenderLayer<T> {

    private final ResourceLocation texture;

    protected TextureLayerGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
        this.texture = null;
    }

    public TextureLayerGeoLayer(GeoRenderer<T> renderer, ResourceLocation texture) {
        super(renderer);

        if (texture == null) throw new IllegalArgumentException("Layer texture cannot be null");

        this.texture = texture;
    }

    @Override
    protected ResourceLocation getTextureResource(T animatable) {
        if (this.texture == null)
            throw new IllegalStateException("A dynamic texture layer must override getTextureResource");

        return this.texture;
    }

    @Override
    public void render(RenderPassInfo<T> renderPassInfo) {
        renderPassInfo.reRender(getTextureResource(renderPassInfo.animatable()));
    }
}
