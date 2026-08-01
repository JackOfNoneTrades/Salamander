package com.geckolib.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.ResourceLocation;

import com.geckolib.GeckoLibConstants;
import com.geckolib.cache.animation.BakedAnimations;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.geckolib.loading.loader.GeckoLibLoader;

/** Atomically published resource caches shared by models and renderers. */
public final class GeckoLibResources {

    public static final String ANIMATIONS_PATH = "geckolib/animations";
    public static final String MODELS_PATH = "geckolib/models";
    public static final String LEGACY_ANIMATIONS_PATH = "animations";
    public static final String LEGACY_MODELS_PATH = "geo";
    public static final Pattern SUFFIX_STRIPPER = Pattern.compile("((\\.geo)|((\\.animation)s?))?(\\.json)$");
    public static final Pattern PREFIX_STRIPPER = Pattern
        .compile("^(?:(?:geckolib/)(?:(?:animations|models)/)?|(?:animations|models|geo)/)");

    private static final List<LoaderEntry> loaders = new ArrayList<>();
    private static volatile CacheSnapshot caches = CacheSnapshot.empty();

    static {
        loaders.add(new LoaderEntry(ignored -> true, new GeckoLibGsonLoader()));
    }

    private GeckoLibResources() {}

    public static BakedAnimationCache getBakedAnimations() {
        return caches.animations;
    }

    public static BakedModelCache getBakedModels() {
        return caches.models;
    }

    public static synchronized void addLoader(GeckoLibLoader.Predicate predicate, GeckoLibLoader loader) {
        loaders.add(0, new LoaderEntry(predicate, loader));
        GeckoLibConstants.LOGGER.info(
            "Added custom resource loader {}",
            loader.getClass()
                .getSimpleName());
    }

    public static synchronized GeckoLibLoader findLoader(ResourceLocation resourcePath) {
        String path = resourcePath.getResourcePath();

        for (LoaderEntry entry : loaders) {
            if (entry.predicate.shouldHandle(resourcePath) && supportsPath(entry.loader, path)) return entry.loader;
        }

        return loaders.get(loaders.size() - 1).loader;
    }

    public static synchronized boolean isFileTypeSupported(String path) {
        for (LoaderEntry entry : loaders) {
            if (supportsPath(entry.loader, path)) return true;
        }

        return false;
    }

    public static synchronized void apply(Map<ResourceLocation, BakedGeoModel> newModels,
        Map<ResourceLocation, BakedAnimations> newAnimations) {
        BakedModelCache modelCache = new BakedModelCache(newModels);
        BakedAnimationCache animationCache = new BakedAnimationCache(newAnimations);

        caches = new CacheSnapshot(modelCache, animationCache);
        GeckoLibConstants.LOGGER.info(
            "Loaded {} geometry models and {} animation files from resources",
            modelCache.size(),
            animationCache.size());
    }

    public static ResourceLocation stripPrefixAndSuffix(ResourceLocation path) {
        String original = path.getResourcePath();
        Matcher prefixMatcher = PREFIX_STRIPPER.matcher(original);
        String stripped = prefixMatcher.find() ? original.substring(prefixMatcher.end()) : original;
        Matcher suffixMatcher = SUFFIX_STRIPPER.matcher(stripped);

        if (suffixMatcher.find()) stripped = stripped.substring(0, suffixMatcher.start());

        return original.equals(stripped) ? path : new ResourceLocation(path.getResourceDomain(), stripped);
    }

    public static boolean isModelResourcePath(String path) {
        return isUnder(path, MODELS_PATH) || isUnder(path, LEGACY_MODELS_PATH);
    }

    public static boolean isAnimationResourcePath(String path) {
        return isUnder(path, ANIMATIONS_PATH) || isUnder(path, LEGACY_ANIMATIONS_PATH);
    }

    public static boolean isLegacyResourcePath(ResourceLocation path) {
        String resourcePath = path.getResourcePath();

        return isUnder(resourcePath, LEGACY_MODELS_PATH) || isUnder(resourcePath, LEGACY_ANIMATIONS_PATH);
    }

    private static boolean supportsPath(GeckoLibLoader loader, String path) {
        for (String extension : loader.supportedExtensions()) {
            if (path.endsWith("." + extension)) return true;
        }

        return false;
    }

    private static boolean isUnder(String path, String root) {
        return path.startsWith(root + "/");
    }

    private static final class LoaderEntry {

        private final GeckoLibLoader.Predicate predicate;
        private final GeckoLibLoader loader;

        private LoaderEntry(GeckoLibLoader.Predicate predicate, GeckoLibLoader loader) {
            this.predicate = predicate;
            this.loader = loader;
        }
    }

    private static final class CacheSnapshot {

        private final BakedModelCache models;
        private final BakedAnimationCache animations;

        private CacheSnapshot(BakedModelCache models, BakedAnimationCache animations) {
            this.models = models;
            this.animations = animations;
        }

        private static CacheSnapshot empty() {
            return new CacheSnapshot(
                new BakedModelCache(Collections.emptyMap()),
                new BakedAnimationCache(Collections.emptyMap()));
        }
    }
}
