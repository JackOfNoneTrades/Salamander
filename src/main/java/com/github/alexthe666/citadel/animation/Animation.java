/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.animation;

/** A fixed-duration Citadel entity animation. */
public class Animation {

    @Deprecated
    private int id;
    private final int duration;

    private Animation(int duration) {
        this.duration = duration;
    }

    /** @deprecated Animation IDs have not been required by Citadel since 1.1. */
    @Deprecated
    public static Animation create(int id, int duration) {
        Animation animation = create(duration);
        animation.id = id;
        return animation;
    }

    public static Animation create(int duration) {
        return new Animation(duration);
    }

    /** @deprecated Animation IDs have not been required by Citadel since 1.1. */
    @Deprecated
    public int getID() {
        return this.id;
    }

    public int getDuration() {
        return this.duration;
    }
}
