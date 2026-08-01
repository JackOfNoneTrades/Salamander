package com.geckolib.animatable.stateless;

import net.minecraft.entity.Entity;

import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.animation.RawAnimation;
import com.geckolib.network.GeckoLibNetwork;

/** Stateless animation API for shared objects such as items and armor. */
public interface StatelessGeoSingletonAnimatable extends StatelessAnimatable, SingletonGeoAnimatable {

    default void playAnimation(String animation, Entity relatedEntity, long instanceId) {
        playAnimation(
            RawAnimation.begin()
                .thenPlay(animation),
            relatedEntity,
            instanceId);
    }

    default void playLoopingAnimation(String animation, Entity relatedEntity, long instanceId) {
        playAnimation(
            RawAnimation.begin()
                .thenLoop(animation),
            relatedEntity,
            instanceId);
    }

    default void playAndHoldAnimation(String animation, Entity relatedEntity, long instanceId) {
        playAnimation(
            RawAnimation.begin()
                .thenPlayAndHold(animation),
            relatedEntity,
            instanceId);
    }

    default void stopAnimation(RawAnimation animation, Entity relatedEntity, long instanceId) {
        stopAnimation(StatelessAnimatable.animationKey(animation), relatedEntity, instanceId);
    }

    default void playAnimation(RawAnimation animation, Entity relatedEntity, long instanceId) {
        if (relatedEntity == null || relatedEntity.worldObj == null) return;

        if (relatedEntity.worldObj.isRemote) handleClientAnimationPlay(this, instanceId, animation);
        else GeckoLibNetwork.playStatelessSingletonAnimation(this, relatedEntity, instanceId, animation);
    }

    default void stopAnimation(String animation, Entity relatedEntity, long instanceId) {
        if (relatedEntity == null || relatedEntity.worldObj == null) return;

        if (relatedEntity.worldObj.isRemote) handleClientAnimationStop(this, instanceId, animation);
        else GeckoLibNetwork.stopStatelessSingletonAnimation(this, relatedEntity, instanceId, animation);
    }

    @Deprecated
    @Override
    default void playAnimation(String animation) {
        throw unsupportedSingletonMethod();
    }

    @Deprecated
    @Override
    default void playLoopingAnimation(String animation) {
        throw unsupportedSingletonMethod();
    }

    @Deprecated
    @Override
    default void playAndHoldAnimation(String animation) {
        throw unsupportedSingletonMethod();
    }

    @Deprecated
    @Override
    default void stopAnimation(RawAnimation animation) {
        throw unsupportedSingletonMethod();
    }

    @Deprecated
    @Override
    default void playAnimation(RawAnimation animation) {
        throw unsupportedSingletonMethod();
    }

    @Deprecated
    @Override
    default void stopAnimation(String animation) {
        throw unsupportedSingletonMethod();
    }

    static IllegalStateException unsupportedSingletonMethod() {
        return new IllegalStateException("Stateless singleton animations require a related entity and instance ID");
    }
}
