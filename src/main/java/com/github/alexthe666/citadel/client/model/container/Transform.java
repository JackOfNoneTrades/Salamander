/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model.container;

public class Transform {

    private float rotationX;
    private float rotationY;
    private float rotationZ;
    private float offsetX;
    private float offsetY;
    private float offsetZ;

    public float getRotationX() {
        return this.rotationX;
    }

    public float getRotationY() {
        return this.rotationY;
    }

    public float getRotationZ() {
        return this.rotationZ;
    }

    public float getOffsetX() {
        return this.offsetX;
    }

    public float getOffsetY() {
        return this.offsetY;
    }

    public float getOffsetZ() {
        return this.offsetZ;
    }

    public void addRotation(float x, float y, float z) {
        this.rotationX += x;
        this.rotationY += y;
        this.rotationZ += z;
    }

    public void addOffset(float x, float y, float z) {
        this.offsetX += x;
        this.offsetY += y;
        this.offsetZ += z;
    }

    public void resetRotation() {
        this.rotationX = 0;
        this.rotationY = 0;
        this.rotationZ = 0;
    }

    public void resetOffset() {
        this.offsetX = 0;
        this.offsetY = 0;
        this.offsetZ = 0;
    }

    public void setRotation(float x, float y, float z) {
        resetRotation();
        addRotation(x, y, z);
    }

    public void setOffset(float x, float y, float z) {
        resetOffset();
        addOffset(x, y, z);
    }
}
