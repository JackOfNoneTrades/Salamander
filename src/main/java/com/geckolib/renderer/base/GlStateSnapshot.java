package com.geckolib.renderer.base;

import java.nio.FloatBuffer;

import net.minecraft.client.renderer.OpenGlHelper;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

/** Narrow explicit state snapshot used to isolate addon-controlled rendering without glPushAttrib. */
public final class GlStateSnapshot {

    private static final ThreadLocal<FloatBuffer> COLOR_BUFFER = ThreadLocal
        .withInitial(() -> BufferUtils.createFloatBuffer(4));

    private final boolean alphaTest = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
    private final boolean blend = GL11.glIsEnabled(GL11.GL_BLEND);
    private final boolean cullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
    private final boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
    private final boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
    private final boolean normalize = GL11.glIsEnabled(GL11.GL_NORMALIZE);
    private final boolean rescaleNormal = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
    private final boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    private final int alphaFunction = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
    private final int blendSource = GL11.glGetInteger(GL11.GL_BLEND_SRC);
    private final int blendDestination = GL11.glGetInteger(GL11.GL_BLEND_DST);
    private final int cullFaceMode = GL11.glGetInteger(GL11.GL_CULL_FACE_MODE);
    private final int depthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
    private final int matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
    private final int shadeModel = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
    private final int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    private final TextureUnitState defaultTexture;
    private final TextureUnitState lightmapTexture;
    private final TextureUnitState initiallyActiveTexture;
    private final float alphaReference = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
    private final float lightmapX = OpenGlHelper.lastBrightnessX;
    private final float lightmapY = OpenGlHelper.lastBrightnessY;
    private final float colorRed;
    private final float colorGreen;
    private final float colorBlue;
    private final float colorAlpha;

    private GlStateSnapshot() {
        this.defaultTexture = TextureUnitState.capture(OpenGlHelper.defaultTexUnit);
        this.lightmapTexture = OpenGlHelper.lightmapTexUnit == OpenGlHelper.defaultTexUnit ? this.defaultTexture
            : TextureUnitState.capture(OpenGlHelper.lightmapTexUnit);
        this.initiallyActiveTexture = this.activeTexture == OpenGlHelper.defaultTexUnit ? this.defaultTexture
            : this.activeTexture == OpenGlHelper.lightmapTexUnit ? this.lightmapTexture
                : TextureUnitState.capture(this.activeTexture);
        OpenGlHelper.setActiveTexture(this.activeTexture);

        FloatBuffer color = COLOR_BUFFER.get();

        color.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);
        this.colorRed = color.get(0);
        this.colorGreen = color.get(1);
        this.colorBlue = color.get(2);
        this.colorAlpha = color.get(3);
    }

    public static GlStateSnapshot capture() {
        return new GlStateSnapshot();
    }

    public void restore() {
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
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, this.lightmapX, this.lightmapY);
        this.defaultTexture.restore();

        if (this.lightmapTexture != this.defaultTexture) this.lightmapTexture.restore();

        if (this.initiallyActiveTexture != this.defaultTexture && this.initiallyActiveTexture != this.lightmapTexture)
            this.initiallyActiveTexture.restore();

        OpenGlHelper.setActiveTexture(this.activeTexture);
        GeoRenderer.setEnabled(GL12.GL_RESCALE_NORMAL, this.rescaleNormal);
        GL11.glColor4f(this.colorRed, this.colorGreen, this.colorBlue, this.colorAlpha);
        GL11.glMatrixMode(this.matrixMode);
    }

    private static final class TextureUnitState {

        private final int textureUnit;
        private final boolean textureEnabled;
        private final int boundTexture;

        private TextureUnitState(int textureUnit) {
            this.textureUnit = textureUnit;
            this.textureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            this.boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        }

        private static TextureUnitState capture(int textureUnit) {
            OpenGlHelper.setActiveTexture(textureUnit);

            return new TextureUnitState(textureUnit);
        }

        private void restore() {
            OpenGlHelper.setActiveTexture(this.textureUnit);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.boundTexture);
            GeoRenderer.setEnabled(GL11.GL_TEXTURE_2D, this.textureEnabled);
        }
    }
}
