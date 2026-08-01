package com.geckolib.animation.state;

import com.geckolib.cache.model.GeoBone;

/** CPU-side pose values for one named bone. */
public final class BoneSnapshot {

    private final String boneName;
    private final GeoBone bone;
    private float scaleX = 1;
    private float scaleY = 1;
    private float scaleZ = 1;
    private float translateX;
    private float translateY;
    private float translateZ;
    private float rotX;
    private float rotY;
    private float rotZ;
    private boolean hidden;
    private boolean childrenHidden;

    private BoneSnapshot(String boneName, GeoBone bone) {
        this.boneName = boneName;
        this.bone = bone;
    }

    public static BoneSnapshot create(String boneName) {
        return new BoneSnapshot(boneName, null);
    }

    public static BoneSnapshot create(GeoBone bone) {
        return new BoneSnapshot(bone.name(), bone);
    }

    public String getBoneName() {
        return this.boneName;
    }

    public GeoBone getBone() {
        return this.bone;
    }

    public float getScaleX() {
        return this.scaleX;
    }

    public float getScaleY() {
        return this.scaleY;
    }

    public float getScaleZ() {
        return this.scaleZ;
    }

    public float getTranslateX() {
        return this.translateX;
    }

    public float getTranslateY() {
        return this.translateY;
    }

    public float getTranslateZ() {
        return this.translateZ;
    }

    public float getRotX() {
        return this.rotX;
    }

    public float getRotY() {
        return this.rotY;
    }

    public float getRotZ() {
        return this.rotZ;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean areChildrenHidden() {
        return this.childrenHidden;
    }

    public BoneSnapshot setScale(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;

        return this;
    }

    public BoneSnapshot setScaleX(float value) {
        this.scaleX = value;

        return this;
    }

    public BoneSnapshot setScaleY(float value) {
        this.scaleY = value;

        return this;
    }

    public BoneSnapshot setScaleZ(float value) {
        this.scaleZ = value;

        return this;
    }

    public BoneSnapshot setTranslation(float x, float y, float z) {
        this.translateX = x;
        this.translateY = y;
        this.translateZ = z;

        return this;
    }

    public BoneSnapshot setTranslateX(float value) {
        this.translateX = value;

        return this;
    }

    public BoneSnapshot setTranslateY(float value) {
        this.translateY = value;

        return this;
    }

    public BoneSnapshot setTranslateZ(float value) {
        this.translateZ = value;

        return this;
    }

    public BoneSnapshot setRotation(float x, float y, float z) {
        this.rotX = x;
        this.rotY = y;
        this.rotZ = z;

        return this;
    }

    public BoneSnapshot setRotX(float value) {
        this.rotX = value;

        return this;
    }

    public BoneSnapshot setRotY(float value) {
        this.rotY = value;

        return this;
    }

    public BoneSnapshot setRotZ(float value) {
        this.rotZ = value;

        return this;
    }

    public BoneSnapshot skipRender(boolean shouldSkip) {
        this.hidden = shouldSkip;

        return this;
    }

    public BoneSnapshot skipChildrenRender(boolean shouldSkip) {
        this.childrenHidden = shouldSkip;

        return this;
    }

    public BoneSnapshot copy() {
        return new BoneSnapshot(this.boneName, this.bone).setScale(this.scaleX, this.scaleY, this.scaleZ)
            .setRotation(this.rotX, this.rotY, this.rotZ)
            .setTranslation(this.translateX, this.translateY, this.translateZ)
            .skipRender(this.hidden)
            .skipChildrenRender(this.childrenHidden);
    }
}
