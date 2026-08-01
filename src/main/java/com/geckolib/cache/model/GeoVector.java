package com.geckolib.cache.model;

import java.util.Objects;

/** Immutable three-component vector used where GeckoLib 5 uses modern Minecraft's {@code Vec3}. */
public final class GeoVector {

    public static final GeoVector ZERO = new GeoVector(0, 0, 0);

    private final double x;
    private final double y;
    private final double z;

    public GeoVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }

    public GeoVector add(double x, double y, double z) {
        return new GeoVector(this.x + x, this.y + y, this.z + z);
    }

    public GeoVector scale(double scale) {
        return multiply(scale, scale, scale);
    }

    public GeoVector multiply(double x, double y, double z) {
        return new GeoVector(this.x * x, this.y * y, this.z * z);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GeoVector)) return false;

        GeoVector other = (GeoVector) obj;

        return Double.compare(this.x, other.x) == 0 && Double.compare(this.y, other.y) == 0
            && Double.compare(this.z, other.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.x, this.y, this.z);
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.y + ", " + this.z + "]";
    }
}
