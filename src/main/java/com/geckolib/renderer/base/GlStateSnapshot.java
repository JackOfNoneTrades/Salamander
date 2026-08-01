package com.geckolib.renderer.base;

import java.nio.FloatBuffer;

import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

/** Narrow explicit state snapshot used to isolate addon render layers without glPushAttrib. */
final class GlStateSnapshot {

    private static final ThreadLocal<FloatBuffer> COLOR_BUFFER = ThreadLocal
        .withInitial(() -> BufferUtils.createFloatBuffer(4));

    private final boolean alphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
    private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
    private final boolean cullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
    private final boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
    private final boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
    private final boolean normalize = GL11.glIsEnabled(GL11.GL_NORMALIZE);
    private final boolean rescaleNormal = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
    private final boolean texture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
    private final boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    private final int alphaFunction = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
    private final int blendSource = GL11.glGetInteger(GL11.GL_BLEND_SRC);
    private final int blendDestination = GL11.glGetInteger(GL11.GL_BLEND_DST);
    private final int cullFaceMode = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
    private final int depthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
    private final int matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
    private final int shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
    private final int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    private final int boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
    private final float alphaReference = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
    private final float lightmapX = OpenGlHelper.lastBrightnessX;
    private final float lightmapY = OpenGlHelper.lastBrightnessY;
    private final float colorRed;
    private final float colorGreen;
    private final float colorBlue;
    private final float colorAlpha;

    private GlStateSnapshot() {
        FloatBuffer color = COLOR_BUFFER.get();

        color.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
        this.colorRed = color.get(0);
        this.colorGreen = color.get(1);
        this.colorBlue = color.get(2);
        this.colorAlpha = color.get(3);
    }

    static GlStateSnapshot capture() {
        return new GlStateSnapshot();
    }

    void restore() {
        GL11.glDepthMask(this.depthMask);
        GL11.glAlphaFunc(this.alphaFunction, this.alphaReference);
        GL11.glBlendFunc(this.blendSource, this.blendDestination);
        GL11.glCullFace(this.cullFaceMode);
        GL11.glDepthFunc(this.depthFunction);
        GL11.glShadeModel(this.shadeModel);
        GeoRenderer.setEnabled(GL11.GL_ALPHA_TEST, this.alphaTest);
        GeoRenderer.setEnabled(GL11.GL_BLEND, this.blend);
        GeoRenderer.setEnabled(GL11.GL_CULL_FACE, this.cullFace);
        GeoRenderer.setEnabled(GL11.GL_DEPTH_TEST, this.depthTest);
        GeoRenderer.setEnabled(GL11.GL_LIGHTING, this.lighting);
        GeoRenderer.setEnabled(GL11.GL_NORMALIZE, this.normalize);
        GeoRenderer.setEnabled(GL12.GL_RESCALE_NORMAL, this.rescaleNormal);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, this.lightmapX, this.lightmapY);
        OpenGlHelper.setActiveTexture(this.activeTexture);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.boundTexture);
        GeoRenderer.setEnabled(GL11.GL_TEXTURE_2D, this.texture);
        GL11.glColor4f(this.colorRed, this.colorGreen, this.colorBlue, this.colorAlpha);
        GL11.glMatrixMode(this.matrixMode);
    }
}
