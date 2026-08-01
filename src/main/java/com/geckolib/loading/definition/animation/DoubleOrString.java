package com.geckolib.loading.definition.animation;

/** A JSON animation value represented by either a number or a Molang expression. */
public abstract class DoubleOrString {

    private DoubleOrString() {}

    public abstract boolean isDouble();

    public double doubleValue() {
        throw new IllegalStateException("This value contains a String");
    }

    public String stringValue() {
        throw new IllegalStateException("This value contains a double");
    }

    public static DoubleOrString of(double value) {
        return new DoubleValue(value);
    }

    public static DoubleOrString of(String value) {
        return new StringValue(value);
    }

    private static final class DoubleValue extends DoubleOrString {

        private final double value;

        private DoubleValue(double value) {
            this.value = value;
        }

        @Override
        public boolean isDouble() {
            return true;
        }

        @Override
        public double doubleValue() {
            return this.value;
        }
    }

    private static final class StringValue extends DoubleOrString {

        private final String value;

        private StringValue(String value) {
            this.value = value;
        }

        @Override
        public boolean isDouble() {
            return false;
        }

        @Override
        public String stringValue() {
            return this.value;
        }
    }
}
