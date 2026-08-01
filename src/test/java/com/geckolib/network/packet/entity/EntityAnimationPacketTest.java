package com.geckolib.network.packet.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class EntityAnimationPacketTest {

    @Test
    public void triggerPacketRoundTripsNullableController() {
        ByteBuf buffer = Unpooled.buffer();

        try {
            EntityAnimTriggerPacket written = new EntityAnimTriggerPacket(42, null, "attack");
            EntityAnimTriggerPacket read = new EntityAnimTriggerPacket();

            written.toBytes(buffer);
            read.fromBytes(buffer);

            assertTrue(read.isValid());
            assertEquals(42, read.entityId());
            assertNull(read.controllerName());
            assertEquals("attack", read.animationName());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void stopPacketRoundTripsNullableNames() {
        ByteBuf buffer = Unpooled.buffer();

        try {
            StopTriggeredEntityAnimPacket written = new StopTriggeredEntityAnimPacket(7, null, null);
            StopTriggeredEntityAnimPacket read = new StopTriggeredEntityAnimPacket();

            written.toBytes(buffer);
            read.fromBytes(buffer);

            assertTrue(read.isValid());
            assertEquals(7, read.entityId());
            assertNull(read.controllerName());
            assertNull(read.animationName());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void rejectsInvalidEntityAndRequiredTriggerName() {
        assertFalse(new EntityAnimTriggerPacket(-1, "action", "attack").isValid());
        assertFalse(new EntityAnimTriggerPacket(1, "action", "").isValid());
        assertFalse(new StopTriggeredEntityAnimPacket(-1, null, null).isValid());
    }
}
