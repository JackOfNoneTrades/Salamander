package com.geckolib.network.packet.singleton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SingletonAnimationPacketTest {

    @Test
    public void triggerPacketRoundTripsNullableController() {
        ByteBuf buffer = Unpooled.buffer();

        try {
            SingletonAnimTriggerPacket written = new SingletonAnimTriggerPacket("example.Item0", 42, null, "open");
            SingletonAnimTriggerPacket read = new SingletonAnimTriggerPacket();

            written.toBytes(buffer);
            read.fromBytes(buffer);

            assertTrue(read.isValid());
            assertEquals("example.Item0", read.syncableId());
            assertEquals(42, read.instanceId());
            assertNull(read.controllerName());
            assertEquals("open", read.animationName());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void stopPacketRoundTripsNullableNames() {
        ByteBuf buffer = Unpooled.buffer();

        try {
            StopTriggeredSingletonAnimPacket written = new StopTriggeredSingletonAnimPacket(
                "example.Item0",
                7,
                null,
                null);
            StopTriggeredSingletonAnimPacket read = new StopTriggeredSingletonAnimPacket();

            written.toBytes(buffer);
            read.fromBytes(buffer);

            assertTrue(read.isValid());
            assertEquals("example.Item0", read.syncableId());
            assertEquals(7, read.instanceId());
            assertNull(read.controllerName());
            assertNull(read.animationName());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void rejectsMissingIdentityZeroInstanceAndRequiredTriggerName() {
        assertFalse(new SingletonAnimTriggerPacket("", 1, "action", "open").isValid());
        assertFalse(new SingletonAnimTriggerPacket("example.Item0", 0, "action", "open").isValid());
        assertFalse(new SingletonAnimTriggerPacket("example.Item0", 1, "action", "").isValid());
        assertFalse(new StopTriggeredSingletonAnimPacket("", 1, null, null).isValid());
    }
}
