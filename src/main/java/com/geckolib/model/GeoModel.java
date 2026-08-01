package com.geckolib.model;

import net.minecraft.util.ResourceLocation;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.cache.GeckoLibResources;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.cache.model.BakedGeoModel;

/** Resource selection API for a GeckoLib model. */
public abstract class GeoModel<T extends GeoAnimatable> {

    public abstract ResourceLocation getModelResource(T animatable);

    public abstract ResourceLocation getTextureResource(T animatable);

    public abstract ResourceLocation getAnimationResource(T animatable);

    public ResourceLocation[] getAnimationResourceFallbacks(T animatable) {
        return new ResourceLocation[0];
    }

    public BakedGeoModel getBakedModel(ResourceLocation location) {
        return GeckoLibResources.getBakedModels()
            .getModel(location);
    }

    public BakedGeoModel getBakedModel(T animatable) {
        return getBakedModel(getModelResource(animatable));
    }

    public BakedAnimations getBakedAnimations(T animatable) {
        return GeckoLibResources.getBakedAnimations()
            .getAnimations(getAnimationResource(animatable), getAnimationResourceFallbacks(animatable));
    }

    public Animation getBakedAnimation(T animatable, String name) {
        return GeckoLibResources.getBakedAnimations()
            .getAnimation(getAnimationResource(animatable), getAnimationResourceFallbacks(animatable), name);
    }
}
