package com.geckolib.network.packet.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class CitadelAnimationPacketTest {

    @Test
    public void roundTripsRegisteredAnimationIndex() {
        CitadelAnimationPacket decoded = roundTrip(new CitadelAnimationPacket(42, 3));

        assertTrue(decoded.isValid());
        assertEquals(42, decoded.entityId());
        assertEquals(3, decoded.animationIndex());
    }

    @Test
    public void acceptsNoAnimationAndRejectsLowerIndices() {
        assertTrue(roundTrip(new CitadelAnimationPacket(42, -1)).isValid());
        assertFalse(roundTrip(new CitadelAnimationPacket(42, -2)).isValid());
    }

    private static CitadelAnimationPacket roundTrip(CitadelAnimationPacket packet) {
        ByteBuf buffer = Unpooled.buffer();
        packet.toBytes(buffer);
        CitadelAnimationPacket decoded = new CitadelAnimationPacket();
        decoded.fromBytes(buffer);
        return decoded;
    }
}
