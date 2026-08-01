/*
 * Derived from Citadel, Copyright (c) AlexModGuy and contributors.
 * SPDX-License-Identifier: LGPL-3.0-only
 * Modified for Minecraft 1.7.10 by the Salamander contributors.
 */
package com.github.alexthe666.citadel.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.Tessellator;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Float-accurate Citadel cuboid geometry rendered through the Minecraft 1.7.10 Tessellator. */
@SideOnly(Side.CLIENT)
public final class TabulaModelRenderUtils {

    private TabulaModelRenderUtils() {}

    private static final class PositionTextureVertex {

        private final float x;
        private final float y;
        private final float z;
        private final float textureU;
        private final float textureV;

        private PositionTextureVertex(float x, float y, float z) {
            this(x, y, z, 0, 0);
        }

        private PositionTextureVertex(float x, float y, float z, float textureU, float textureV) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.textureU = textureU;
            this.textureV = textureV;
        }

        private PositionTextureVertex withTexture(float textureU, float textureV) {
            return new PositionTextureVertex(this.x, this.y, this.z, textureU, textureV);
        }
    }

    private static final class TexturedQuad {

        private PositionTextureVertex[] vertices;
        private final float normalX;
        private final float normalY;
        private final float normalZ;

        private TexturedQuad(PositionTextureVertex[] vertices, float minU, float minV, float maxU, float maxV,
            float textureWidth, float textureHeight, boolean mirror, float normalX, float normalY, float normalZ) {
            this.vertices = vertices;
            this.vertices[0] = this.vertices[0].withTexture(maxU / textureWidth, minV / textureHeight);
            this.vertices[1] = this.vertices[1].withTexture(minU / textureWidth, minV / textureHeight);
            this.vertices[2] = this.vertices[2].withTexture(minU / textureWidth, maxV / textureHeight);
            this.vertices[3] = this.vertices[3].withTexture(maxU / textureWidth, maxV / textureHeight);

            if (mirror) reverse(this.vertices);

            this.normalX = mirror ? -normalX : normalX;
            this.normalY = normalY;
            this.normalZ = normalZ;
        }

        private void draw(Tessellator tessellator, float scale) {
            tessellator.startDrawingQuads();
            tessellator.setNormal(this.normalX, this.normalY, this.normalZ);

            for (PositionTextureVertex vertex : this.vertices) {
                tessellator.addVertexWithUV(
                    vertex.x * scale,
                    vertex.y * scale,
                    vertex.z * scale,
                    vertex.textureU,
                    vertex.textureV);
            }

            tessellator.draw();
        }

        private static void reverse(PositionTextureVertex[] vertices) {
            for (int left = 0, right = vertices.length - 1; left < right; left++, right--) {
                PositionTextureVertex vertex = vertices[left];
                vertices[left] = vertices[right];
                vertices[right] = vertex;
            }
        }
    }

    public static class ModelBox {

        private final TexturedQuad[] quads;
        public final float posX1;
        public final float posY1;
        public final float posZ1;
        public final float posX2;
        public final float posY2;
        public final float posZ2;

        public ModelBox(int textureX, int textureY, float x, float y, float z, float width, float height, float depth,
            float deltaX, float deltaY, float deltaZ, boolean mirror, float textureWidth, float textureHeight) {
            this.posX1 = x;
            this.posY1 = y;
            this.posZ1 = z;
            this.posX2 = x + width;
            this.posY2 = y + height;
            this.posZ2 = z + depth;

            float maxX = this.posX2;
            float maxY = this.posY2;
            float maxZ = this.posZ2;
            x -= deltaX;
            y -= deltaY;
            z -= deltaZ;
            maxX += deltaX;
            maxY += deltaY;
            maxZ += deltaZ;

            if (mirror) {
                float swappedX = maxX;
                maxX = x;
                x = swappedX;
            }

            PositionTextureVertex minMinMin = new PositionTextureVertex(x, y, z);
            PositionTextureVertex maxMinMin = new PositionTextureVertex(maxX, y, z);
            PositionTextureVertex maxMaxMin = new PositionTextureVertex(maxX, maxY, z);
            PositionTextureVertex minMaxMin = new PositionTextureVertex(x, maxY, z);
            PositionTextureVertex minMinMax = new PositionTextureVertex(x, y, maxZ);
            PositionTextureVertex maxMinMax = new PositionTextureVertex(maxX, y, maxZ);
            PositionTextureVertex maxMaxMax = new PositionTextureVertex(maxX, maxY, maxZ);
            PositionTextureVertex minMaxMax = new PositionTextureVertex(x, maxY, maxZ);

            float u0 = textureX;
            float u1 = u0 + depth;
            float u2 = u1 + width;
            float u3 = u2 + width;
            float u4 = u2 + depth;
            float u5 = u4 + width;
            float v0 = textureY;
            float v1 = v0 + depth;
            float v2 = v1 + height;

            this.quads = new TexturedQuad[6];
            this.quads[2] = quad(
                maxMinMax,
                minMinMax,
                minMinMin,
                maxMinMin,
                u1,
                v0,
                u2,
                v1,
                textureWidth,
                textureHeight,
                mirror,
                0,
                -1,
                0);
            this.quads[3] = quad(
                maxMaxMin,
                minMaxMin,
                minMaxMax,
                maxMaxMax,
                u2,
                v1,
                u3,
                v0,
                textureWidth,
                textureHeight,
                mirror,
                0,
                1,
                0);
            this.quads[1] = quad(
                minMinMin,
                minMinMax,
                minMaxMax,
                minMaxMin,
                u0,
                v1,
                u1,
                v2,
                textureWidth,
                textureHeight,
                mirror,
                -1,
                0,
                0);
            this.quads[4] = quad(
                maxMinMin,
                minMinMin,
                minMaxMin,
                maxMaxMin,
                u1,
                v1,
                u2,
                v2,
                textureWidth,
                textureHeight,
                mirror,
                0,
                0,
                -1);
            this.quads[0] = quad(
                maxMinMax,
                maxMinMin,
                maxMaxMin,
                maxMaxMax,
                u2,
                v1,
                u4,
                v2,
                textureWidth,
                textureHeight,
                mirror,
                1,
                0,
                0);
            this.quads[5] = quad(
                minMinMax,
                maxMinMax,
                maxMaxMax,
                minMaxMax,
                u4,
                v1,
                u5,
                v2,
                textureWidth,
                textureHeight,
                mirror,
                0,
                0,
                1);
        }

        public void render(Tessellator tessellator, float scale) {
            for (TexturedQuad quad : this.quads) quad.draw(tessellator, scale);
        }

        private static TexturedQuad quad(PositionTextureVertex first, PositionTextureVertex second,
            PositionTextureVertex third, PositionTextureVertex fourth, float minU, float minV, float maxU, float maxV,
            float textureWidth, float textureHeight, boolean mirror, float normalX, float normalY, float normalZ) {
            return new TexturedQuad(
                new PositionTextureVertex[] { first, second, third, fourth },
                minU,
                minV,
                maxU,
                maxV,
                textureWidth,
                textureHeight,
                mirror,
                normalX,
                normalY,
                normalZ);
        }
    }

    public static final class LegacyModelBox extends net.minecraft.client.model.ModelBox {

        private final ModelBox delegate;

        public LegacyModelBox(ModelRenderer owner, int textureX, int textureY, float x, float y, float z, float width,
            float height, float depth, float deltaX, float deltaY, float deltaZ, boolean mirror, float textureWidth,
            float textureHeight) {
            super(owner, textureX, textureY, x, y, z, 0, 0, 0, 0);
            this.delegate = new ModelBox(
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
                textureWidth,
                textureHeight);
        }

        @Override
        public void render(Tessellator tessellator, float scale) {
            this.delegate.render(tessellator, scale);
        }
    }
}
