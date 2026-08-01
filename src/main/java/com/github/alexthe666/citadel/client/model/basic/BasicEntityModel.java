/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model.basic;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Minecraft 1.7.10 render boundary for Citadel's non-final model-part hierarchy. */
@SideOnly(Side.CLIENT)
public abstract class BasicEntityModel<T extends Entity> extends ModelBase {

    protected BasicEntityModel() {}

    public abstract Iterable<BasicModelPart> parts();

    public abstract void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
        float headPitch);

    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {}

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
        float headPitch, float scale) {
        setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);

        for (BasicModelPart part : parts()) part.render(scale);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
        float headPitch, float scale, Entity entity) {
        setupAnim((T) entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount,
        float partialTick) {
        prepareMobModel((T) entity, limbSwing, limbSwingAmount, partialTick);
    }
}
