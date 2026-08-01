package com.geckolib.renderer.layer;

import net.minecraft.util.ResourceLocation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Base class for ordered supplementary render passes over an already-posed model. */
@SideOnly(Side.CLIENT)
public abstract class GeoRenderLayer<T extends GeoAnimatable> {

    protected final GeoRenderer<T> renderer;

    protected GeoRenderLayer(GeoRenderer<T> renderer) {
        if (renderer == null) throw new IllegalArgumentException("Renderer cannot be null");

        this.renderer = renderer;
    }

    public GeoModel<T> getGeoModel() {
        return this.renderer.getGeoModel();
    }

    public BakedGeoModel getDefaultBakedModel(T animatable) {
        return getGeoModel().getBakedModel(animatable);
    }

    public GeoRenderer<T> getRenderer() {
        return this.renderer;
    }

    protected ResourceLocation getTextureResource(T animatable) {
        return getGeoModel().getTextureResource(animatable);
    }

    /**
     * Called in layer order before transform capture and the base model render.
     *
     * <p>
     * Layers may adjust the supplied per-render pose here. Transform lookups are not available until
     * {@link #render(RenderPassInfo)}.
     */
    public void preRender(RenderPassInfo<T> renderPassInfo) {}

    /** Called in layer order after the base model has rendered. */
    public void render(RenderPassInfo<T> renderPassInfo) {}
}
