package com.geckolib.animatable;

import net.minecraft.entity.Entity;

import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.network.GeckoLibNetwork;

/** GeckoLib animatable contract for Minecraft entities. */
public interface GeoEntity extends GeoAnimatable {

    /**
     * Trigger a registered animation. Server calls are synchronized to the entity's tracking clients; client calls are
     * local visual changes only.
     *
     * @param controllerName controller to target, or {@code null} to search controllers in registration order
     * @param animationName  trigger name registered through {@code triggerableAnim}
     */
    default void triggerAnim(String controllerName, String animationName) {
        Entity entity = (Entity) this;

        if (entity.worldObj == null) return;

        if (!entity.worldObj.isRemote) {
            GeckoLibNetwork.triggerEntityAnimation(entity, controllerName, animationName);
            return;
        }

        AnimatableManager<GeoEntity> manager = getAnimatableInstanceCache().getManagerForId(entity.getEntityId());

        if (controllerName == null) {
            manager.tryTriggerAnimation(animationName);
        } else {
            manager.tryTriggerAnimation(controllerName, animationName);
        }
    }

    /**
     * Stop a registered triggered animation. Both names may be {@code null}; a null controller searches in registration
     * order and a null animation accepts whichever trigger is currently active.
     */
    default void stopTriggeredAnim(String controllerName, String animationName) {
        Entity entity = (Entity) this;

        if (entity.worldObj == null) return;

        if (!entity.worldObj.isRemote) {
            GeckoLibNetwork.stopTriggeredEntityAnimation(entity, controllerName, animationName);
            return;
        }

        AnimatableManager<GeoEntity> manager = getAnimatableInstanceCache().getManagerForId(entity.getEntityId());

        if (controllerName == null) {
            manager.stopTriggeredAnimation(animationName);
        } else {
            manager.stopTriggeredAnimation(controllerName, animationName);
        }
    }
}
