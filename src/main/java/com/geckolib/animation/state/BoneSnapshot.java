package com.geckolib.animation.state;

/** CPU-side pose values for one named bone. */
public final class BoneSnapshot {

    private final String boneName;
    private float scaleX = 1;
    private float scaleY = 1;
    private float scaleZ = 1;
    private float translateX;
    private float translateY;
    private float translateZ;
    private float rotX;
    private float rotY;
    private float rotZ;

    private BoneSnapshot(String boneName) {
        this.boneName = boneName;
    }

    public static BoneSnapshot create(String boneName) {
        return new BoneSnapshot(boneName);
    }

    public String getBoneName() {
        return this.boneName;
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

    public BoneSnapshot setScale(float x, float y, float z) {
        this.scaleX = x;
        this.scaleY = y;
        this.scaleZ = z;

        return this;
    }

    public BoneSnapshot setTranslation(float x, float y, float z) {
        this.translateX = x;
        this.translateY = y;
        this.translateZ = z;

        return this;
    }

    public BoneSnapshot setRotation(float x, float y, float z) {
        this.rotX = x;
        this.rotY = y;
        this.rotZ = z;

        return this;
    }
}
