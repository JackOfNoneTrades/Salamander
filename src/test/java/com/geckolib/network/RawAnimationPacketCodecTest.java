package com.geckolib.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class RawAnimationPacketCodecTest {

    @Test
    public void roundTripsAllBuiltInStageTypes() {
        RawAnimation written = RawAnimation.begin()
            .thenPlay("animation.test.open")
            .thenWait(7)
            .thenLoop("animation.test.idle")
            .thenPlayAndHold("animation.test.close");
        ByteBuf buffer = Unpooled.buffer();

        try {
            RawAnimationPacketCodec.write(buffer, written);

            RawAnimation read = RawAnimationPacketCodec.read(buffer);

            assertEquals(written, read);
            assertEquals(
                "RawAnimation{animation.test.open -> internal.wait -> animation.test.idle -> animation.test.close}",
                read.toString());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void rejectsEmptyOversizedAndUnregisteredAnimations() {
        RawAnimation tooManyStages = RawAnimation.begin();

        for (int i = 0; i <= RawAnimationPacketCodec.MAX_STAGES; i++) {
            tooManyStages.thenPlay("animation.test." + i);
        }

        LoopType unregistered = animation -> false;

        assertFalse(RawAnimationPacketCodec.isValid(RawAnimation.begin()));
        assertFalse(RawAnimationPacketCodec.isValid(tooManyStages));
        assertFalse(
            RawAnimationPacketCodec.isValid(
                RawAnimation.begin()
                    .then("animation.test.custom", unregistered)));
        assertTrue(
            RawAnimationPacketCodec.isValid(
                RawAnimation.begin()
                    .thenPlay("animation.test.valid")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOversizedStageCountBeforeReadingStages() {
        ByteBuf buffer = Unpooled.buffer();

        try {
            buffer.writeByte(RawAnimationPacketCodec.MAX_STAGES + 1);
            RawAnimationPacketCodec.read(buffer);
        } finally {
            buffer.release();
        }
    }
}
