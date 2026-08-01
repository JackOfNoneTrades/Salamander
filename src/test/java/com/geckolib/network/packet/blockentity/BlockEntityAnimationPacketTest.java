package com.geckolib.network.packet.blockentity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BlockEntityAnimationPacketTest {

    @Test
    public void triggerPacketRoundTripsPositionAndNullableController() {
        ByteBuf buffer = Unpooled.buffer();

        try {
            BlockEntityAnimTriggerPacket written = new BlockEntityAnimTriggerPacket(12, 64, -9, null, "deploy");
            BlockEntityAnimTriggerPacket read = new BlockEntityAnimTriggerPacket();

            written.toBytes(buffer);
            read.fromBytes(buffer);

            assertTrue(read.isValid());
            assertEquals(12, read.x());
            assertEquals(64, read.y());
            assertEquals(-9, read.z());
            assertNull(read.controllerName());
            assertEquals("deploy", read.animationName());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void stopPacketRoundTripsNullableNames() {
        ByteBuf buffer = Unpooled.buffer();

        try {
            StopTriggeredBlockEntityAnimPacket written = new StopTriggeredBlockEntityAnimPacket(1, 2, 3, null, null);
            StopTriggeredBlockEntityAnimPacket read = new StopTriggeredBlockEntityAnimPacket();

            written.toBytes(buffer);
            read.fromBytes(buffer);

            assertTrue(read.isValid());
            assertEquals(1, read.x());
            assertEquals(2, read.y());
            assertEquals(3, read.z());
            assertNull(read.controllerName());
            assertNull(read.animationName());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void rejectsEmptyNames() {
        assertFalse(new BlockEntityAnimTriggerPacket(1, 2, 3, "action", "").isValid());
        assertFalse(new BlockEntityAnimTriggerPacket(1, 2, 3, "", "deploy").isValid());
        assertFalse(new StopTriggeredBlockEntityAnimPacket(1, 2, 3, "", null).isValid());
        assertFalse(new StopTriggeredBlockEntityAnimPacket(1, 2, 3, null, "").isValid());
    }
}
