/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model.basic;

import java.util.Random;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

import com.github.alexthe666.citadel.client.model.TabulaModelRenderUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Extensible Citadel model part with float-dimension cube support. */
@SideOnly(Side.CLIENT)
public class BasicModelPart extends ModelRenderer {

    public int textureOffsetX;
    public int textureOffsetY;

    public BasicModelPart(BasicEntityModel<?> model) {
        super(model);
    }

    public BasicModelPart(BasicEntityModel<?> model, int textureOffsetX, int textureOffsetY) {
        super(model, textureOffsetX, textureOffsetY);
    }

    public BasicModelPart(int textureWidth, int textureHeight, int textureOffsetX, int textureOffsetY) {
        super(new EmptyModel(), textureOffsetX, textureOffsetY);
        setTextureSize(textureWidth, textureHeight);
    }

    private BasicModelPart() {
        super(new EmptyModel());
    }

    public BasicModelPart getModelAngleCopy() {
        BasicModelPart copy = new BasicModelPart();
        copy.copyModelAngles(this);
        return copy;
    }

    public void copyModelAngles(BasicModelPart source) {
        this.rotateAngleX = source.rotateAngleX;
        this.rotateAngleY = source.rotateAngleY;
        this.rotateAngleZ = source.rotateAngleZ;
        this.rotationPointX = source.rotationPointX;
        this.rotationPointY = source.rotationPointY;
        this.rotationPointZ = source.rotationPointZ;
    }

    public void addChild(BasicModelPart child) {
        super.addChild(child);
    }

    @Override
    public BasicModelPart setTextureOffset(int x, int y) {
        this.textureOffsetX = x;
        this.textureOffsetY = y;
        super.setTextureOffset(x, y);
        return this;
    }

    public BasicModelPart addBox(String partName, float x, float y, float z, int width, int height, int depth,
        float delta, int textureX, int textureY) {
        addCitadelBox(textureX, textureY, x, y, z, width, height, depth, delta, delta, delta, this.mirror);
        return this;
    }

    public BasicModelPart addBox(float x, float y, float z, float width, float height, float depth) {
        addCitadelBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, 0, 0, 0, this.mirror);
        return this;
    }

    public BasicModelPart addBox(float x, float y, float z, float width, float height, float depth, boolean mirror) {
        addCitadelBox(this.textureOffsetX, this.textureOffsetY, x, y, z, width, height, depth, 0, 0, 0, mirror);
        return this;
    }

    public void addBox(float x, float y, float z, float width, float height, float depth, float delta) {
        addCitadelBox(
            this.textureOffsetX,
            this.textureOffsetY,
            x,
            y,
            z,
            width,
            height,
            depth,
            delta,
            delta,
            delta,
            this.mirror);
    }

    public void addBox(float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY,
        float deltaZ) {
        addCitadelBox(
            this.textureOffsetX,
            this.textureOffsetY,
            x,
            y,
            z,
            width,
            height,
            depth,
            deltaX,
            deltaY,
            deltaZ,
            this.mirror);
    }

    public void addBox(float x, float y, float z, float width, float height, float depth, float delta, boolean mirror) {
        addCitadelBox(
            this.textureOffsetX,
            this.textureOffsetY,
            x,
            y,
            z,
            width,
            height,
            depth,
            delta,
            delta,
            delta,
            mirror);
    }

    public void setPos(float x, float y, float z) {
        setRotationPoint(x, y, z);
    }

    @Override
    public BasicModelPart setTextureSize(int textureWidth, int textureHeight) {
        super.setTextureSize(textureWidth, textureHeight);
        return this;
    }

    public ModelBox getRandomCube(Random random) {
        return this.cubeList.isEmpty() ? null : this.cubeList.get(random.nextInt(this.cubeList.size()));
    }

    protected final void addCitadelBox(int textureX, int textureY, float x, float y, float z, float width, float height,
        float depth, float deltaX, float deltaY, float deltaZ, boolean mirror) {
        this.cubeList.add(
            new TabulaModelRenderUtils.LegacyModelBox(
                this,
                textureX,
                textureY,
                x,
                y,
                z,
                width,
                height,
                depth,
                deltaX,
                deltaY,
                deltaZ,
                mirror,
                this.textureWidth,
                this.textureHeight));
    }

    private static final class EmptyModel extends ModelBase {
    }
}
