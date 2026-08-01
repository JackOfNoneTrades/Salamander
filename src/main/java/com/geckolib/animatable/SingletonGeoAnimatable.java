package com.geckolib.animatable;

import net.minecraft.entity.Entity;

import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.cache.SyncedSingletonAnimatableCache;
import com.geckolib.network.GeckoLibNetwork;

/** GeckoLib animatable contract for shared singleton objects such as items. */
public interface SingletonGeoAnimatable extends GeoAnimatable {

    static void registerSyncedAnimatable(SingletonGeoAnimatable animatable) {
        SyncedSingletonAnimatableCache.registerSyncedAnimatable(animatable);
    }

    /**
     * Trigger a registered animation. Server calls are synchronized to clients tracking the related entity; client
     * calls are local visual changes only.
     */
    default void triggerAnim(Entity relatedEntity, long instanceId, String controllerName, String animationName) {
        if (relatedEntity == null || relatedEntity.worldObj == null) return;

        if (!relatedEntity.worldObj.isRemote) {
            GeckoLibNetwork.triggerSingletonAnimation(this, relatedEntity, instanceId, controllerName, animationName);
            return;
        }

        AnimatableManager<SingletonGeoAnimatable> manager = getAnimatableInstanceCache().getManagerForId(instanceId);

        if (controllerName == null) manager.tryTriggerAnimation(animationName);
        else manager.tryTriggerAnimation(controllerName, animationName);
    }

    /** Trigger an animation for an equipped armor stack using its assigned positive item ID. */
    default void triggerArmorAnim(Entity relatedEntity, long instanceId, String controllerName, String animationName) {
        triggerAnim(
            relatedEntity,
            instanceId == Long.MIN_VALUE ? Long.MAX_VALUE : -instanceId,
            controllerName,
            animationName);
    }

    /** Stop a registered triggered animation locally or on clients tracking the related entity. */
    default void stopTriggeredAnim(Entity relatedEntity, long instanceId, String controllerName, String animationName) {
        if (relatedEntity == null || relatedEntity.worldObj == null) return;

        if (!relatedEntity.worldObj.isRemote) {
            GeckoLibNetwork
                .stopTriggeredSingletonAnimation(this, relatedEntity, instanceId, controllerName, animationName);
            return;
        }

        AnimatableManager<SingletonGeoAnimatable> manager = getAnimatableInstanceCache().getManagerForId(instanceId);

        if (controllerName == null) manager.stopTriggeredAnimation(animationName);
        else manager.stopTriggeredAnimation(controllerName, animationName);
    }

    /** Stop an equipped armor animation using its assigned positive item ID. */
    default void stopTriggeredArmorAnim(Entity relatedEntity, long instanceId, String controllerName,
        String animationName) {
        stopTriggeredAnim(
            relatedEntity,
            instanceId == Long.MIN_VALUE ? Long.MAX_VALUE : -instanceId,
            controllerName,
            animationName);
    }

    @Override
    default AnimatableInstanceCache animatableCacheOverride() {
        return new SingletonAnimatableInstanceCache(this);
    }
}
