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
        ResourceLocation[] fallbacks = fallbackFiles == null ? new ResourceLocation[0] : fallbackFiles;
        BakedAnimations animations = null;

        for (int i = -1; i < fallbacks.length; i++) {
            ResourceLocation path = i < 0 ? animationFile : fallbacks[i];

            animations = this.cache.get(path);

            if (animations == null) animations = this.cache.get(GeckoLibResources.stripPrefixAndSuffix(path));

            if (animations != null) {
                Animation animation = animations.getAnimation(animationName);

                if (animation != null) return animation;
            }
        }

        if (animations == null) GeckoLibConstants.LOGGER.error("Unable to find animation file '{}'", animationFile);
        else GeckoLibConstants.LOGGER
            .error("Unable to find animation '{}' in animation file '{}'", animationName, animationFile);

        return null;
    }
}
