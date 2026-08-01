package com.geckolib.renderer.layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.renderer.base.GeoRenderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Ordered render-layer collection owned by one renderer. */
@SideOnly(Side.CLIENT)
public final class GeoRenderLayersContainer<T extends GeoAnimatable> {

    private final GeoRenderer<T> renderer;
    private final List<GeoRenderLayer<T>> layers = new ArrayList<>();
    private final List<GeoRenderLayer<T>> readOnlyLayers = Collections.unmodifiableList(this.layers);

    public GeoRenderLayersContainer(GeoRenderer<T> renderer) {
        this.renderer = renderer;
    }

    public List<GeoRenderLayer<T>> getRenderLayers() {
        return this.readOnlyLayers;
    }

    public void addLayer(GeoRenderLayer<T> layer) {
        if (layer == null) throw new IllegalArgumentException("Render layer cannot be null");

        if (layer.getRenderer() != this.renderer)
            throw new IllegalArgumentException("Render layer belongs to a different renderer");

        this.layers.add(layer);
    }

    public boolean removeLayer(GeoRenderLayer<T> layer) {
        return this.layers.remove(layer);
    }
}
