package com.geckolib.util;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.InstancedAnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animation.object.EasingType;
import com.geckolib.animation.object.LoopType;
import com.geckolib.cache.GeckoLibResources;
import com.geckolib.loading.loader.GeckoLibLoader;
import com.geckolib.loading.math.MathParser;
import com.geckolib.loading.math.function.MathFunction;

/** Public registration and cache helpers matching GeckoLib's common entry points. */
public final class GeckoLibUtil {

    private GeckoLibUtil() {}

    public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable) {
        AnimatableInstanceCache override = animatable.animatableCacheOverride();

        return override == null ? new InstancedAnimatableInstanceCache(animatable) : override;
    }

    public static AnimatableInstanceCache createInstanceCache(GeoAnimatable animatable, boolean singletonObject) {
        AnimatableInstanceCache override = animatable.animatableCacheOverride();

        if (override != null) return override;

        return singletonObject ? new SingletonAnimatableInstanceCache(animatable)
            : new InstancedAnimatableInstanceCache(animatable);
    }

    public static <T extends EasingType> T addCustomEasingType(String name, T easingType) {
        return EasingType.register(name, easingType);
    }

    public static <T extends LoopType> T addCustomLoopType(String name, T loopType) {
        return LoopType.register(name, loopType);
    }

    public static void addCustomMathFunction(String name, MathFunction.Factory<?> factory) {
        MathParser.registerFunction(name, factory);
    }

    public static void addResourceLoader(GeckoLibLoader.Predicate predicate, GeckoLibLoader loader) {
        GeckoLibResources.addLoader(predicate, loader);
    }
}
