/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model;

import net.minecraft.entity.Entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@FunctionalInterface
@SideOnly(Side.CLIENT)
public interface ITabulaModelAnimator<T extends Entity> {

    void setRotationAngles(TabulaModel model, T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
        float rotationYaw, float rotationPitch, float scale);
}
