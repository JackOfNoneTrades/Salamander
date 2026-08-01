package com.geckolib.cache.animation.keyframeevent;

import java.util.Objects;

/** Custom instruction marker. */
public final class CustomInstructionKeyframeData extends KeyFrameData {

    private final String instructions;

    public CustomInstructionKeyframeData(double time, String instructions) {
        super(time, null);
        this.instructions = instructions;
    }

    public String getInstructions() {
        return this.instructions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTime(), this.instructions);
    }
}
