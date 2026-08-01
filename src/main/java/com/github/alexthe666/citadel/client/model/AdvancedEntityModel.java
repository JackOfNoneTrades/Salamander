/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.model.TextureOffset;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Citadel model base shared by Alex's Mobs and Ice and Fire style models. */
@SideOnly(Side.CLIENT)
public abstract class AdvancedEntityModel<T extends Entity> extends BasicEntityModel<T> {

    public int texWidth = 32;
    public int texHeight = 32;

    private float movementScale = 1;
    private final Map<String, com.github.alexthe666.citadel.client.model.container.TextureOffset> modelTextureMap = new HashMap<>();

    public void updateDefaultPose() {
        for (AdvancedModelBox part : getAllParts()) part.updateDefaultPose();
    }

    @Override
    protected void setTextureOffset(String partName, int x, int y) {
        this.modelTextureMap
            .put(partName, new com.github.alexthe666.citadel.client.model.container.TextureOffset(x, y));
    }

    public com.github.alexthe666.citadel.client.model.container.TextureOffset getTextureOffset(String partName) {
        return this.modelTextureMap.get(partName);
    }

    TextureOffset getVanillaTextureOffset(String partName) {
        com.github.alexthe666.citadel.client.model.container.TextureOffset textureOffset = getTextureOffset(partName);
        return textureOffset == null ? null
            : new TextureOffset(textureOffset.textureOffsetX, textureOffset.textureOffsetY);
    }

    public void resetToDefaultPose() {
        for (AdvancedModelBox part : getAllParts()) part.resetToDefaultPose();
    }

    public void faceTarget(float yaw, float pitch, float rotationDivisor, AdvancedModelBox... boxes) {
        float actualRotationDivisor = rotationDivisor * boxes.length;
        float yawAmount = (float) Math.toRadians(yaw) / actualRotationDivisor;
        float pitchAmount = (float) Math.toRadians(pitch) / actualRotationDivisor;

        for (AdvancedModelBox box : boxes) {
            box.rotateAngleY += yawAmount;
            box.rotateAngleX += pitchAmount;
        }
    }

    public void chainSwing(AdvancedModelBox[] boxes, float speed, float degree, double rootOffset, float swing,
        float swingAmount) {
        float offset = calculateChainOffset(rootOffset, boxes);

        for (int index = 0; index < boxes.length; index++) {
            boxes[index].rotateAngleY += calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
        }
    }

    public void chainWave(AdvancedModelBox[] boxes, float speed, float degree, double rootOffset, float swing,
        float swingAmount) {
        float offset = calculateChainOffset(rootOffset, boxes);

        for (int index = 0; index < boxes.length; index++) {
            boxes[index].rotateAngleX += calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
        }
    }

    public void chainFlap(AdvancedModelBox[] boxes, float speed, float degree, double rootOffset, float swing,
        float swingAmount) {
        float offset = calculateChainOffset(rootOffset, boxes);

        for (int index = 0; index < boxes.length; index++) {
            boxes[index].rotateAngleZ += calculateChainRotation(speed, degree, swing, swingAmount, offset, index);
        }
    }

    public float getMovementScale() {
        return this.movementScale;
    }

    public void setMovementScale(float movementScale) {
        this.movementScale = movementScale;
    }

    public void walk(AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight,
        float walk, float walkAmount) {
        box.walk(speed, degree, invert, offset, weight, walk, walkAmount);
    }

    public void flap(AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight,
        float flap, float flapAmount) {
        box.flap(speed, degree, invert, offset, weight, flap, flapAmount);
    }

    public void swing(AdvancedModelBox box, float speed, float degree, boolean invert, float offset, float weight,
        float swing, float swingAmount) {
        box.swing(speed, degree, invert, offset, weight, swing, swingAmount);
    }

    public void bob(AdvancedModelBox box, float speed, float degree, boolean bounce, float movement,
        float movementAmount) {
        box.bob(speed, degree, bounce, movement, movementAmount);
    }

    public float moveBox(float speed, float degree, boolean bounce, float movement, float movementAmount) {
        if (bounce) return -MathHelper.abs(MathHelper.sin(movement * speed) * movementAmount * degree);
        return MathHelper.sin(movement * speed) * movementAmount * degree - movementAmount * degree;
    }

    public void setRotateAngle(AdvancedModelBox model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void rotate(ModelAnimator animator, AdvancedModelBox model, float x, float y, float z) {
        animator.rotate(model, (float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
    }

    public void rotateMinus(ModelAnimator animator, AdvancedModelBox model, float x, float y, float z) {
        animator.rotate(
            model,
            (float) Math.toRadians(x) - model.defaultRotationX,
            (float) Math.toRadians(y) - model.defaultRotationY,
            (float) Math.toRadians(z) - model.defaultRotationZ);
    }

    public void progressRotation(AdvancedModelBox model, float progress, float rotationX, float rotationY,
        float rotationZ, float divisor) {
        model.rotateAngleX += progress * (rotationX - model.defaultRotationX) / divisor;
        model.rotateAngleY += progress * (rotationY - model.defaultRotationY) / divisor;
        model.rotateAngleZ += progress * (rotationZ - model.defaultRotationZ) / divisor;
    }

    public void progressRotationPrev(AdvancedModelBox model, float progress, float rotationX, float rotationY,
        float rotationZ, float divisor) {
        model.rotateAngleX += progress * rotationX / divisor;
        model.rotateAngleY += progress * rotationY / divisor;
        model.rotateAngleZ += progress * rotationZ / divisor;
    }

    public void progressPosition(AdvancedModelBox model, float progress, float x, float y, float z, float divisor) {
        model.rotationPointX += progress * (x - model.defaultPositionX) / divisor;
        model.rotationPointY += progress * (y - model.defaultPositionY) / divisor;
        model.rotationPointZ += progress * (z - model.defaultPositionZ) / divisor;
    }

    public void progressPositionPrev(AdvancedModelBox model, float progress, float x, float y, float z, float divisor) {
        model.rotationPointX += progress * x / divisor;
        model.rotationPointY += progress * y / divisor;
        model.rotationPointZ += progress * z / divisor;
    }

    public abstract Iterable<AdvancedModelBox> getAllParts();

    private float calculateChainRotation(float speed, float degree, float swing, float swingAmount, float offset,
        int boxIndex) {
        return MathHelper.cos(swing * speed * this.movementScale + offset * boxIndex) * swingAmount
            * degree
            * this.movementScale;
    }

    private static float calculateChainOffset(double rootOffset, AdvancedModelBox... boxes) {
        return (float) (rootOffset * Math.PI / (2 * boxes.length));
    }
}
