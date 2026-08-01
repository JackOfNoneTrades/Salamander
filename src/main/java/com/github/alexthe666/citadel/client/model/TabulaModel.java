/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

import com.github.alexthe666.citadel.client.model.basic.BasicModelPart;
import com.github.alexthe666.citadel.client.model.container.TabulaCubeContainer;
import com.github.alexthe666.citadel.client.model.container.TabulaCubeGroupContainer;
import com.github.alexthe666.citadel.client.model.container.TabulaModelContainer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Runtime model created from a Tabula {@code model.json} container. */
@Deprecated
@SideOnly(Side.CLIENT)
public class TabulaModel extends AdvancedEntityModel<Entity> {

    protected final Map<String, AdvancedModelBox> cubes = new HashMap<>();
    protected final List<AdvancedModelBox> rootBoxes = new ArrayList<>();
    protected final Map<String, AdvancedModelBox> identifierMap = new HashMap<>();
    protected ITabulaModelAnimator<Entity> tabulaAnimator;
    public final ModelAnimator llibAnimator;

    private final double[] modelScale;

    @SuppressWarnings("unchecked")
    public TabulaModel(TabulaModelContainer container, ITabulaModelAnimator<?> tabulaAnimator) {
        this.texWidth = container.getTextureWidth();
        this.texHeight = container.getTextureHeight();
        this.textureWidth = this.texWidth;
        this.textureHeight = this.texHeight;
        this.tabulaAnimator = (ITabulaModelAnimator<Entity>) tabulaAnimator;

        for (TabulaCubeContainer cube : container.getCubes()) parseCube(cube, null);
        for (TabulaCubeGroupContainer group : container.getCubeGroups()) parseCubeGroup(group);

        updateDefaultPose();
        double[] scale = container.getScale();
        this.modelScale = scale == null || scale.length < 3 ? new double[] { 1, 1, 1 } : scale;
        this.llibAnimator = ModelAnimator.create();
    }

    public TabulaModel(TabulaModelContainer container) {
        this(container, null);
    }

    @Override
    public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw,
        float rotationPitch, float scale) {
        setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch);
        GL11.glPushMatrix();
        GL11.glScaled(this.modelScale[0], this.modelScale[1], this.modelScale[2]);

        for (AdvancedModelBox rootBox : this.rootBoxes) rootBox.render(scale);

        GL11.glPopMatrix();
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float rotationYaw,
        float rotationPitch) {
        if (this.tabulaAnimator != null) {
            this.tabulaAnimator
                .setRotationAngles(this, entity, limbSwing, limbSwingAmount, ageInTicks, rotationYaw, rotationPitch, 1);
        }
    }

    public AdvancedModelBox getCube(String name) {
        return this.cubes.get(name);
    }

    public AdvancedModelBox getCubeByIdentifier(String identifier) {
        return this.identifierMap.get(identifier);
    }

    public Map<String, AdvancedModelBox> getCubes() {
        return this.cubes;
    }

    @Override
    public Iterable<BasicModelPart> parts() {
        return new ArrayList<BasicModelPart>(this.rootBoxes);
    }

    @Override
    public Iterable<AdvancedModelBox> getAllParts() {
        return new ArrayList<>(this.cubes.values());
    }

    private void parseCubeGroup(TabulaCubeGroupContainer group) {
        for (TabulaCubeContainer cube : group.getCubes()) parseCube(cube, null);
        for (TabulaCubeGroupContainer child : group.getCubeGroups()) parseCubeGroup(child);
    }

    private void parseCube(TabulaCubeContainer cube, AdvancedModelBox parent) {
        AdvancedModelBox box = createBox(cube);
        this.cubes.put(cube.getName(), box);
        this.identifierMap.put(cube.getIdentifier(), box);

        if (parent == null) this.rootBoxes.add(box);
        else parent.addChild(box);

        for (TabulaCubeContainer child : cube.getChildren()) parseCube(child, box);
    }

    private AdvancedModelBox createBox(TabulaCubeContainer cube) {
        int[] textureOffset = cube.getTextureOffset();
        double[] position = cube.getPosition();
        double[] rotation = cube.getRotation();
        double[] offset = cube.getOffset();
        int[] dimensions = cube.getDimensions();
        AdvancedModelBox box = new AdvancedModelBox(this, cube.getName());
        box.setTextureOffset(textureOffset[0], textureOffset[1]);
        box.mirror = cube.isTextureMirrorEnabled();
        box.setPos((float) position[0], (float) position[1], (float) position[2]);
        box.addBox(
            (float) offset[0],
            (float) offset[1],
            (float) offset[2],
            dimensions[0],
            dimensions[1],
            dimensions[2],
            0);
        box.rotateAngleX = (float) Math.toRadians(rotation[0]);
        box.rotateAngleY = (float) Math.toRadians(rotation[1]);
        box.rotateAngleZ = (float) Math.toRadians(rotation[2]);
        return box;
    }
}
