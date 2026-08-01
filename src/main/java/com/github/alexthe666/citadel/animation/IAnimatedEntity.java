/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.animation;

/** Animation state implemented by entities using Citadel keyframed models. */
public interface IAnimatedEntity {

    Animation NO_ANIMATION = Animation.create(0);

    int getAnimationTick();

    void setAnimationTick(int tick);

    Animation getAnimation();

    void setAnimation(Animation animation);

    Animation[] getAnimations();
}
