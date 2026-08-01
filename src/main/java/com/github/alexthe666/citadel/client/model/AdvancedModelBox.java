/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.TextureOffset;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import com.github.alexthe666.citadel.client.model.basic.BasicModelPart;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Citadel's hierarchical, scalable model part adapted to the 1.7.10 renderer. */
@SideOnly(Side.CLIENT)
public class AdvancedModelBox extends BasicModelPart {

    public float defaultRotationX;
    public float defaultRotationY;
    public float defaultRotationZ;
    public float defaultOffsetX;
    public float defaultOffsetY;
    public float defaultOffsetZ;
    public float defaultPositionX;
    public float defaultPositionY;
    public float defaultPositionZ;
    public float scaleX = 1;
    public float scaleY = 1;
    public float scaleZ = 1;
    public boolean scaleChildren;
    public final List<TabulaModelRenderUtils.ModelBox> cubeList = new ArrayList<>();
    public final List<BasicModelPart> childModels = new ArrayList<>();
    public String boxName;

    private final AdvancedEntityModel<?> model;
    private AdvancedModelBox parent;
    private int displayList;
    private boolean compiled;

    public AdvancedModelBox(AdvancedEntityModel<?> model, String name) {
        super(model);
        this.model = model;
        this.boxName = name;
        setTexSize(model.texWidth, model.texHeight);
    }

    public AdvancedModelBox(AdvancedEntityModel<?> model) {
        this(model, null);
    }

    public AdvancedModelBox(AdvancedEntityModel<?> model, int textureOffsetX, int textureOffsetY) {
        this(model);
        setTextureOffset(textureOffsetX, textureOffsetY);
    }

    public BasicModelPart setTexSize(int textureWidth, int textureHeight) {
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        return this;
    }

    @Override
    public AdvancedModelBox addBox(String name, float x, float y, float z, int width, int height, int depth) {
        String fullName = this.boxName + "." + name;
        TextureOffset textureOffset = this.model.getVanillaTextureOffset(fullName);

        if (textureOffset != null) setTextureOffset(textureOffset.textureOffsetX, textureOffset.textureOffsetY);

        addAdvancedBox(x, y, z, width, height, depth, 0, 0, 0, this.mirror);
        return this;
    }

    public BasicModelPart addBox(String name, float x, float y, float z, int width, int height, int depth, float delta,
        int textureX, int textureY) {
        setTextureOffset(textureX, textureY);
        addAdvancedBox(x, y, z, width, height, depth, delta, delta, delta, this.mirror);
        return this;
    }

    @Override
    public AdvancedModelBox addBox(float x, float y, float z, int width, int height, int depth) {
        addAdvancedBox(x, y, z, width, height, depth, 0, 0, 0, this.mirror);
        return this;
    }

    @Override
    public void addBox(float x, float y, float z, int width, int height, int depth, float delta) {
        addAdvancedBox(x, y, z, width, height, depth, delta, delta, delta, this.mirror);
    }

    @Override
    public AdvancedModelBox addBox(float x, float y, float z, float width, float height, float depth) {
        addAdvancedBox(x, y, z, width, height, depth, 0, 0, 0, this.mirror);
        return this;
    }

    @Override
    public AdvancedModelBox addBox(float x, float y, float z, float width, float height, float depth, boolean mirror) {
        addAdvancedBox(x, y, z, width, height, depth, 0, 0, 0, mirror);
        return this;
    }

    @Override
    public void addBox(float x, float y, float z, float width, float height, float depth, float delta) {
        addAdvancedBox(x, y, z, width, height, depth, delta, delta, delta, this.mirror);
    }

    @Override
    public void addBox(float x, float y, float z, float width, float height, float depth, float deltaX, float deltaY,
        float deltaZ) {
        addAdvancedBox(x, y, z, width, height, depth, deltaX, deltaY, deltaZ, this.mirror);
    }

    @Override
    public void addBox(float x, float y, float z, float width, float height, float depth, float delta, boolean mirror) {
        addAdvancedBox(x, y, z, width, height, depth, delta, delta, delta, mirror);
    }

    public void setShouldScaleChildren(boolean scaleChildren) {
        this.scaleChildren = scaleChildren;
    }

    public void setScale(float scaleX, float scaleY, float scaleZ) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public void setScaleZ(float scaleZ) {
        this.scaleZ = scaleZ;
    }

    public void updateDefaultPose() {
        this.defaultRotationX = this.rotateAngleX;
        this.defaultRotationY = this.rotateAngleY;
        this.defaultRotationZ = this.rotateAngleZ;
        this.defaultPositionX = this.rotationPointX;
        this.defaultPositionY = this.rotationPointY;
        this.defaultPositionZ = this.rotationPointZ;
    }

    @Override
    public void setPos(float x, float y, float z) {
        setRotationPoint(x, y, z);
    }

    public void resetToDefaultPose() {
        this.rotateAngleX = this.defaultRotationX;
        this.rotateAngleY = this.defaultRotationY;
        this.rotateAngleZ = this.defaultRotationZ;
        this.rotationPointX = this.defaultPositionX;
        this.rotationPointY = this.defaultPositionY;
        this.rotationPointZ = this.defaultPositionZ;
    }

    @Override
    public void addChild(BasicModelPart child) {
        this.childModels.add(child);

        if (child instanceof AdvancedModelBox) ((AdvancedModelBox) child).setParent(this);
    }

    @Override
    public void addChild(ModelRenderer child) {
        if (child instanceof BasicModelPart) addChild((BasicModelPart) child);
        else super.addChild(child);
    }

    public AdvancedModelBox getParent() {
        return this.parent;
    }

    public void setParent(AdvancedModelBox parent) {
        this.parent = parent;
    }

    public void parentedPostRender(float scale) {
        if (this.parent != null) this.parent.parentedPostRender(scale);
        postRender(scale);
    }

    public void renderWithParents(float scale) {
        if (this.parent != null) this.parent.renderWithParents(scale);
        render(scale);
    }

    @Override
    public void render(float scale) {
        if (this.isHidden || !this.showModel) return;
        if (!this.compiled) compileDisplayList(scale);

        GL11.glPushMatrix();
        applyTransform(scale, true);
        GL11.glCallList(this.displayList);

        if (!this.scaleChildren && isScaled()) {
            GL11.glScalef(inverseScale(this.scaleX), inverseScale(this.scaleY), inverseScale(this.scaleZ));
        }

        for (BasicModelPart child : this.childModels) child.render(scale);

        GL11.glPopMatrix();
    }

    @Override
    public void postRender(float scale) {
        if (this.isHidden || !this.showModel) return;
        applyTransform(scale, true);
    }

    public AdvancedEntityModel<?> getModel() {
        return this.model;
    }

    public void walk(float speed, float degree, boolean invert, float offset, float weight, float walk,
        float walkAmount) {
        this.rotateAngleX += calculateRotation(speed, degree, invert, offset, weight, walk, walkAmount);
    }

    public void flap(float speed, float degree, boolean invert, float offset, float weight, float flap,
        float flapAmount) {
        this.rotateAngleZ += calculateRotation(speed, degree, invert, offset, weight, flap, flapAmount);
    }

    public void swing(float speed, float degree, boolean invert, float offset, float weight, float swing,
        float swingAmount) {
        this.rotateAngleY += calculateRotation(speed, degree, invert, offset, weight, swing, swingAmount);
    }

    public void bob(float speed, float degree, boolean bounce, float movement, float movementAmount) {
        float movementScale = this.model.getMovementScale();
        float bob = MathHelper.sin(movement * speed * movementScale) * movementAmount * degree * movementScale
            - movementAmount * degree * movementScale;

        if (bounce)
            bob = -Math.abs(MathHelper.sin(movement * speed * movementScale) * movementAmount * degree * movementScale);

        this.rotationPointY += bob;
    }

    @Override
    public AdvancedModelBox setTextureOffset(int textureOffsetX, int textureOffsetY) {
        super.setTextureOffset(textureOffsetX, textureOffsetY);
        return this;
    }

    public void transitionTo(AdvancedModelBox target, float timer, float maxTime) {
        this.rotateAngleX += (target.rotateAngleX - this.rotateAngleX) / maxTime * timer;
        this.rotateAngleY += (target.rotateAngleY - this.rotateAngleY) / maxTime * timer;
        this.rotateAngleZ += (target.rotateAngleZ - this.rotateAngleZ) / maxTime * timer;
        this.rotationPointX += (target.rotationPointX - this.rotationPointX) / maxTime * timer;
        this.rotationPointY += (target.rotationPointY - this.rotationPointY) / maxTime * timer;
        this.rotationPointZ += (target.rotationPointZ - this.rotationPointZ) / maxTime * timer;
        this.offsetX += (target.offsetX - this.offsetX) / maxTime * timer;
        this.offsetY += (target.offsetY - this.offsetY) / maxTime * timer;
        this.offsetZ += (target.offsetZ - this.offsetZ) / maxTime * timer;
    }

    private void addAdvancedBox(float x, float y, float z, float width, float height, float depth, float deltaX,
        float deltaY, float deltaZ, boolean mirror) {
        this.cubeList.add(
            new TabulaModelRenderUtils.ModelBox(
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
                mirror,
                this.textureWidth,
                this.textureHeight));
    }

    private void compileDisplayList(float scale) {
        this.displayList = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(this.displayList, GL11.GL_COMPILE);

        for (TabulaModelRenderUtils.ModelBox cube : this.cubeList) {
            cube.render(Tessellator.instance, scale);
        }

        GL11.glEndList();
        this.compiled = true;
    }

    private void applyTransform(float scale, boolean applyScale) {
        GL11.glTranslatef(this.offsetX, this.offsetY, this.offsetZ);
        GL11.glTranslatef(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);

        if (this.rotateAngleZ != 0) GL11.glRotatef((float) Math.toDegrees(this.rotateAngleZ), 0, 0, 1);
        if (this.rotateAngleY != 0) GL11.glRotatef((float) Math.toDegrees(this.rotateAngleY), 0, 1, 0);
        if (this.rotateAngleX != 0) GL11.glRotatef((float) Math.toDegrees(this.rotateAngleX), 1, 0, 0);
        if (applyScale && isScaled()) GL11.glScalef(this.scaleX, this.scaleY, this.scaleZ);
    }

    private float calculateRotation(float speed, float degree, boolean invert, float offset, float weight,
        float movement, float movementAmount) {
        float movementScale = this.model.getMovementScale();
        float rotation = MathHelper.cos(movement * speed * movementScale + offset) * degree
            * movementScale
            * movementAmount + weight * movementAmount;
        return invert ? -rotation : rotation;
    }

    private boolean isScaled() {
        return this.scaleX != 1 || this.scaleY != 1 || this.scaleZ != 1;
    }

    private static float inverseScale(float scale) {
        return 1 / Math.max(scale, 0.0001F);
    }
}
