/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model.container;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class TextureOffset extends net.minecraft.client.model.TextureOffset {

    public TextureOffset(int textureOffsetX, int textureOffsetY) {
        super(textureOffsetX, textureOffsetY);
    }
}
