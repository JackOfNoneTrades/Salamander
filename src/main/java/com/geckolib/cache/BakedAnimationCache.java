package com.geckolib.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import com.geckolib.GeckoLibConstants;
import com.geckolib.cache.animation.Animation;
import com.geckolib.cache.animation.BakedAnimations;

/** Immutable snapshot of animation resources loaded during the latest resource reload. */
public final class BakedAnimationCache {

    private final Map<ResourceLocation, BakedAnimations> cache;

    public BakedAnimationCache(Map<ResourceLocation, BakedAnimations> cache) {
        this.cache = Collections.unmodifiableMap(new LinkedHashMap<>(cache));
    }

    public Map<ResourceLocation, BakedAnimations> cache() {
        return this.cache;
    }

    public int size() {
        return this.cache.size();
    }

    public Animation getAnimation(ResourceLocation animationFile, ResourceLocation[] fallbackFiles,
        String animationName) {
        BakedAnimations animations = getAnimations(animationFile, fallbackFiles);

        if (animations.isEmpty()) {
            GeckoLibConstants.LOGGER.error("Unable to find animation file '{}'", animationFile);

            return null;
        }

        Animation animation = animations.getAnimation(animationName);

        if (animation == null) GeckoLibConstants.LOGGER
            .error("Unable to find animation '{}' in animation file '{}'", animationName, animationFile);

        return animation;
    }

    public BakedAnimations getAnimations(ResourceLocation animationFile, ResourceLocation[] fallbackFiles) {
        ResourceLocation[] fallbacks = fallbackFiles == null ? new ResourceLocation[0] : fallbackFiles;

        if (fallbacks.length == 0) {
            BakedAnimations animations = findAnimations(animationFile);

            return animations == null ? BakedAnimations.empty() : animations;
        }

        Map<String, Animation> animations = new LinkedHashMap<>();

        for (int i = fallbacks.length - 1; i >= -1; i--) {
            ResourceLocation path = i < 0 ? animationFile : fallbacks[i];
            BakedAnimations bakedAnimations = findAnimations(path);

            if (bakedAnimations != null) animations.putAll(bakedAnimations.animations());
        }

        return animations.isEmpty() ? BakedAnimations.empty() : new BakedAnimations(animations);
    }

    private BakedAnimations findAnimations(ResourceLocation path) {
        BakedAnimations animations = this.cache.get(path);

        return animations == null ? this.cache.get(GeckoLibResources.stripPrefixAndSuffix(path)) : animations;
    }
}
