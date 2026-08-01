package com.geckolib.animatable;

import net.minecraft.entity.Entity;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.network.GeckoLibNetwork;

/** Shared animatable used to replace the renderer of existing entity classes. */
public interface GeoReplacedEntity extends SingletonGeoAnimatable {

    /** Trigger an animation for one replaced entity instance. */
    default void triggerAnim(Entity relatedEntity, String controllerName, String animationName) {
        if (relatedEntity == null || relatedEntity.worldObj == null) return;

        if (!relatedEntity.worldObj.isRemote) {
            GeckoLibNetwork.triggerReplacedEntityAnimation(this, relatedEntity, controllerName, animationName);
            return;
        }

        AnimatableManager<GeoReplacedEntity> manager = getAnimatableInstanceCache()
            .getManagerForId(relatedEntity.getEntityId());

        if (controllerName == null) manager.tryTriggerAnimation(animationName);
        else manager.tryTriggerAnimation(controllerName, animationName);
    }

    /** Stop a triggered animation for one replaced entity instance. */
    default void stopTriggeredAnim(Entity relatedEntity, String controllerName, String animationName) {
        if (relatedEntity == null || relatedEntity.worldObj == null) return;

        if (!relatedEntity.worldObj.isRemote) {
            GeckoLibNetwork.stopTriggeredReplacedEntityAnimation(this, relatedEntity, controllerName, animationName);
            return;
        }

        AnimatableManager<GeoReplacedEntity> manager = getAnimatableInstanceCache()
            .getManagerForId(relatedEntity.getEntityId());

        if (controllerName == null) manager.stopTriggeredAnimation(animationName);
        else manager.stopTriggeredAnimation(controllerName, animationName);
    }
}
