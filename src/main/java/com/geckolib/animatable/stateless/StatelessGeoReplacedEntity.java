package com.geckolib.animatable.stateless;

import net.minecraft.entity.Entity;

import com.geckolib.animatable.GeoReplacedEntity;
import com.geckolib.animation.RawAnimation;
import com.geckolib.network.GeckoLibNetwork;

/** Stateless animation API for a shared replacement animatable and one related entity. */
public interface StatelessGeoReplacedEntity extends StatelessGeoSingletonAnimatable, GeoReplacedEntity {

    default void playAnimation(String animation, Entity relatedEntity) {
        playAnimation(animation, relatedEntity, relatedEntity.getEntityId());
    }

    default void playLoopingAnimation(String animation, Entity relatedEntity) {
        playLoopingAnimation(animation, relatedEntity, relatedEntity.getEntityId());
    }

    default void playAndHoldAnimation(String animation, Entity relatedEntity) {
        playAndHoldAnimation(animation, relatedEntity, relatedEntity.getEntityId());
    }

    default void stopAnimation(RawAnimation animation, Entity relatedEntity) {
        stopAnimation(animation, relatedEntity, relatedEntity.getEntityId());
    }

    default void playAnimation(RawAnimation animation, Entity relatedEntity) {
        playAnimation(animation, relatedEntity, relatedEntity.getEntityId());
    }

    default void stopAnimation(String animation, Entity relatedEntity) {
        stopAnimation(animation, relatedEntity, relatedEntity.getEntityId());
    }

    @Override
    default void playAnimation(RawAnimation animation, Entity relatedEntity, long instanceId) {
        if (relatedEntity == null || relatedEntity.worldObj == null) return;
        if (instanceId < Integer.MIN_VALUE || instanceId > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Replaced entity animation ID is outside the 1.7 entity ID range");

        if (relatedEntity.worldObj.isRemote) handleClientAnimationPlay(this, instanceId, animation);
        else GeckoLibNetwork.playStatelessEntityAnimation(relatedEntity, (int) instanceId, true, animation);
    }

    @Override
    default void stopAnimation(String animation, Entity relatedEntity, long instanceId) {
        if (relatedEntity == null || relatedEntity.worldObj == null) return;
        if (instanceId < Integer.MIN_VALUE || instanceId > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Replaced entity animation ID is outside the 1.7 entity ID range");

        if (relatedEntity.worldObj.isRemote) handleClientAnimationStop(this, instanceId, animation);
        else GeckoLibNetwork.stopStatelessEntityAnimation(relatedEntity, (int) instanceId, true, animation);
    }
}
