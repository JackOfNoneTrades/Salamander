package com.geckolib.loading.loader;

import java.io.Reader;

import net.minecraft.util.ResourceLocation;

import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.math.MathParser;

/** Common-side extension point for alternate GeckoLib resource formats. */
public interface GeckoLibLoader {

    String[] supportedExtensions();

    BakedGeoModel loadModel(ResourceLocation resourcePath, Reader reader);

    BakedAnimations loadAnimations(ResourceLocation resourcePath, Reader reader, MathParser mathParser);

    @FunctionalInterface
    interface Predicate {

        boolean shouldHandle(ResourceLocation resourcePath);
    }
}
