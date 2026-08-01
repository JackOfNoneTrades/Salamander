package com.geckolib.network;

import java.nio.charset.StandardCharsets;

import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;

import io.netty.buffer.ByteBuf;

/** Bounded network encoding for a server-selected raw animation chain. */
public final class RawAnimationPacketCodec {

    public static final int MAX_STAGES = 32;
    public static final int MAX_ANIMATION_NAME_LENGTH = 256;
    public static final int MAX_LOOP_TYPE_ID_LENGTH = 64;
    public static final int MAX_ANIMATION_KEY_LENGTH = 14 + MAX_STAGES * MAX_ANIMATION_NAME_LENGTH
        + (MAX_STAGES - 1) * 4;

    private static final int MAX_ANIMATION_NAME_BYTES = MAX_ANIMATION_NAME_LENGTH * 4;
    private static final int MAX_LOOP_TYPE_ID_BYTES = MAX_LOOP_TYPE_ID_LENGTH * 4;
    private static final String DEFAULT_LOOP_TYPE_ID = "default";

    private RawAnimationPacketCodec() {}

    public static void write(ByteBuf buffer, RawAnimation animation) {
        if (!isValid(animation)) throw new IllegalArgumentException("Invalid stateless raw animation");

        buffer.writeByte(animation.getStageCount());

        for (RawAnimation.Stage stage : animation.getAnimationStages()) {
            writeString(buffer, stage.animationName(), MAX_ANIMATION_NAME_LENGTH, MAX_ANIMATION_NAME_BYTES);
            writeString(buffer, loopTypeId(stage.loopType()), MAX_LOOP_TYPE_ID_LENGTH, MAX_LOOP_TYPE_ID_BYTES);
            buffer.writeInt(stage.waitTicks());
        }
    }

    public static RawAnimation read(ByteBuf buffer) {
        int stageCount = buffer.readUnsignedByte();

        if (stageCount == 0 || stageCount > MAX_STAGES)
            throw new IllegalArgumentException("Invalid stateless animation stage count: " + stageCount);

        RawAnimation animation = RawAnimation.begin();

        for (int i = 0; i < stageCount; i++) {
            String animationName = readString(buffer, MAX_ANIMATION_NAME_LENGTH, MAX_ANIMATION_NAME_BYTES);
            String loopTypeId = readString(buffer, MAX_LOOP_TYPE_ID_LENGTH, MAX_LOOP_TYPE_ID_BYTES);
            int waitTicks = buffer.readInt();
            LoopType loopType = resolveLoopType(loopTypeId);

            if (RawAnimation.Stage.WAIT.equals(animationName)) {
                if (loopType != LoopType.PLAY_ONCE || waitTicks < 0)
                    throw new IllegalArgumentException("Invalid stateless wait animation stage");

                animation.thenWait(waitTicks);
            } else {
                if (waitTicks != 0) throw new IllegalArgumentException("Non-wait animation stage has a wait duration");

                animation.then(animationName, loopType);
            }
        }

        return animation;
    }

    public static boolean isValid(RawAnimation animation) {
        if (animation == null || animation.getStageCount() == 0 || animation.getStageCount() > MAX_STAGES) return false;

        for (RawAnimation.Stage stage : animation.getAnimationStages()) {
            if (!isValidString(stage.animationName(), MAX_ANIMATION_NAME_LENGTH, MAX_ANIMATION_NAME_BYTES))
                return false;

            if (RawAnimation.Stage.WAIT.equals(stage.animationName())) {
                if (stage.loopType() != LoopType.PLAY_ONCE || stage.waitTicks() < 0) return false;
            } else if (stage.waitTicks() != 0) {
                return false;
            }

            String loopTypeId = loopTypeId(stage.loopType());

            if (!isValidString(loopTypeId, MAX_LOOP_TYPE_ID_LENGTH, MAX_LOOP_TYPE_ID_BYTES)) return false;
        }

        return true;
    }

    public static boolean isValidAnimationKey(String animationKey) {
        return animationKey != null && !animationKey.isEmpty() && animationKey.length() <= MAX_ANIMATION_KEY_LENGTH;
    }

    public static void writeAnimationKey(ByteBuf buffer, String animationKey) {
        writeString(buffer, animationKey, MAX_ANIMATION_KEY_LENGTH, MAX_ANIMATION_KEY_LENGTH * 4);
    }

    public static String readAnimationKey(ByteBuf buffer) {
        return readString(buffer, MAX_ANIMATION_KEY_LENGTH, MAX_ANIMATION_KEY_LENGTH * 4);
    }

    private static String loopTypeId(LoopType loopType) {
        if (loopType == LoopType.DEFAULT) return DEFAULT_LOOP_TYPE_ID;
        if (loopType == LoopType.PLAY_ONCE) return "play_once";
        if (loopType == LoopType.HOLD_ON_LAST_FRAME) return "hold_on_last_frame";
        if (loopType == LoopType.LOOP) return "loop";

        try {
            return loopType == null ? null : loopType.getId();
        } catch (IllegalStateException ignored) {
            return null;
        }
    }

    private static LoopType resolveLoopType(String id) {
        if (DEFAULT_LOOP_TYPE_ID.equals(id)) return LoopType.DEFAULT;

        LoopType loopType = LoopType.LOOP_TYPES.get(id);

        if (loopType == null) throw new IllegalArgumentException("Unknown stateless animation loop type: " + id);

        return loopType;
    }

    private static void writeString(ByteBuf buffer, String value, int maxCharacters, int maxBytes) {
        if (!isValidString(value, maxCharacters, maxBytes))
            throw new IllegalArgumentException("Invalid bounded packet string");

        byte[] encoded = value.getBytes(StandardCharsets.UTF_8);

        buffer.writeShort(encoded.length);
        buffer.writeBytes(encoded);
    }

    private static String readString(ByteBuf buffer, int maxCharacters, int maxBytes) {
        int byteLength = buffer.readUnsignedShort();

        if (byteLength == 0 || byteLength > maxBytes || byteLength > buffer.readableBytes())
            throw new IllegalArgumentException("Invalid bounded packet string length: " + byteLength);

        String value = buffer.toString(buffer.readerIndex(), byteLength, StandardCharsets.UTF_8);

        buffer.skipBytes(byteLength);

        if (!isValidString(value, maxCharacters, maxBytes))
            throw new IllegalArgumentException("Invalid bounded packet string contents");

        return value;
    }

    private static boolean isValidString(String value, int maxCharacters, int maxBytes) {
        return value != null && !value.isEmpty()
            && value.length() <= maxCharacters
            && value.getBytes(StandardCharsets.UTF_8).length <= maxBytes;
    }
}
