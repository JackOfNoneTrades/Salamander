/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.animation;

import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;

import com.geckolib.network.GeckoLibNetwork;

/** Server-authoritative Citadel animation state and synchronization. */
public enum AnimationHandler {

    INSTANCE;

    public <T extends Entity & IAnimatedEntity> void sendAnimationMessage(T entity, Animation animation) {
        if (entity.worldObj.isRemote) return;

        int animationIndex = indexOf(entity.getAnimations(), animation);

        if (animation != IAnimatedEntity.NO_ANIMATION && animationIndex < 0) {
            throw new IllegalArgumentException("Animation is not registered by the animated entity");
        }

        entity.setAnimation(animation);
        entity.setAnimationTick(0);
        GeckoLibNetwork.syncCitadelAnimation(entity, animationIndex);
    }

    public <T extends Entity & IAnimatedEntity> void updateAnimations(T entity) {
        Animation animation = entity.getAnimation();

        if (animation == null) {
            entity.setAnimation(IAnimatedEntity.NO_ANIMATION);
            entity.setAnimationTick(0);
            return;
        }

        if (animation == IAnimatedEntity.NO_ANIMATION) return;

        if (entity.getAnimationTick() == 0) {
            AnimationEvent.Start<T> event = new AnimationEvent.Start<>(entity, animation);

            if (!MinecraftForge.EVENT_BUS.post(event)) {
                sendAnimationMessage(entity, event.getAnimation());
                animation = entity.getAnimation();
            }
        }

        if (entity.getAnimationTick() < animation.getDuration()) {
            entity.setAnimationTick(entity.getAnimationTick() + 1);
            MinecraftForge.EVENT_BUS.post(new AnimationEvent.Tick<>(entity, animation, entity.getAnimationTick()));
        }

        if (entity.getAnimationTick() == animation.getDuration()) {
            entity.setAnimationTick(0);
            entity.setAnimation(IAnimatedEntity.NO_ANIMATION);
        }
    }

    private static int indexOf(Animation[] animations, Animation animation) {
        if (animation == IAnimatedEntity.NO_ANIMATION) return -1;
        if (animations == null) return -1;

        for (int index = 0; index < animations.length; index++) {
            if (animations[index] == animation) return index;
        }

        return -1;
    }
}
