package com.geckolib.network.packet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.geckolib.animatable.stateless.StatelessAnimatable;
import com.geckolib.animation.RawAnimation;
import com.geckolib.network.packet.blockentity.StatelessBlockEntityPlayAnimPacket;
import com.geckolib.network.packet.blockentity.StatelessBlockEntityStopAnimPacket;
import com.geckolib.network.packet.entity.StatelessEntityPlayAnimPacket;
import com.geckolib.network.packet.entity.StatelessEntityStopAnimPacket;
import com.geckolib.network.packet.singleton.StatelessSingletonPlayAnimPacket;
import com.geckolib.network.packet.singleton.StatelessSingletonStopAnimPacket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class StatelessAnimationPacketTest {

    private static final RawAnimation ANIMATION = RawAnimation.begin()
        .thenPlay("animation.test.open")
        .thenWait(4)
        .thenLoop("animation.test.idle");

    @Test
    public void entityPacketsRoundTripReplacedRoutingAndAnimation() {
        ByteBuf buffer = Unpooled.buffer();

        try {
            StatelessEntityPlayAnimPacket written = new StatelessEntityPlayAnimPacket(42, true, ANIMATION);
            StatelessEntityPlayAnimPacket read = new StatelessEntityPlayAnimPacket();

            written.toBytes(buffer);
            read.fromBytes(buffer);

            assertTrue(read.isValid());
            assertEquals(42, read.entityId());
            assertTrue(read.isReplacedEntity());
            assertEquals(ANIMATION, read.animation());

            buffer.clear();

            StatelessEntityStopAnimPacket writtenStop = new StatelessEntityStopAnimPacket(42, true, "open");
            StatelessEntityStopAnimPacket readStop = new StatelessEntityStopAnimPacket();

            writtenStop.toBytes(buffer);
            readStop.fromBytes(buffer);

            assertTrue(readStop.isValid());
            assertTrue(readStop.isReplacedEntity());
            assertEquals("open", readStop.animation());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void blockEntityPacketsRoundTripPositionAndAnimation() {
        ByteBuf buffer = Unpooled.buffer();

        try {
            StatelessBlockEntityPlayAnimPacket written = new StatelessBlockEntityPlayAnimPacket(4, 70, -9, ANIMATION);
            StatelessBlockEntityPlayAnimPacket read = new StatelessBlockEntityPlayAnimPacket();

            written.toBytes(buffer);
            read.fromBytes(buffer);

            assertTrue(read.isValid());
            assertEquals(4, read.x());
            assertEquals(70, read.y());
            assertEquals(-9, read.z());
            assertEquals(ANIMATION, read.animation());

            buffer.clear();

            StatelessBlockEntityStopAnimPacket writtenStop = new StatelessBlockEntityStopAnimPacket(4, 70, -9, "open");
            StatelessBlockEntityStopAnimPacket readStop = new StatelessBlockEntityStopAnimPacket();

            writtenStop.toBytes(buffer);
            readStop.fromBytes(buffer);

            assertTrue(readStop.isValid());
            assertEquals("open", readStop.animation());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void singletonPacketsRoundTripIdentityInstanceAndAnimation() {
        ByteBuf buffer = Unpooled.buffer();

        try {
            StatelessSingletonPlayAnimPacket written = new StatelessSingletonPlayAnimPacket(
                "example.Item0",
                -27,
                ANIMATION);
            StatelessSingletonPlayAnimPacket read = new StatelessSingletonPlayAnimPacket();

            written.toBytes(buffer);
            read.fromBytes(buffer);

            assertTrue(read.isValid());
            assertEquals("example.Item0", read.syncableId());
            assertEquals(-27, read.instanceId());
            assertEquals(ANIMATION, read.animation());

            buffer.clear();

            StatelessSingletonStopAnimPacket writtenStop = new StatelessSingletonStopAnimPacket(
                "example.Item0",
                -27,
                "open");
            StatelessSingletonStopAnimPacket readStop = new StatelessSingletonStopAnimPacket();

            writtenStop.toBytes(buffer);
            readStop.fromBytes(buffer);

            assertTrue(readStop.isValid());
            assertEquals("open", readStop.animation());
        } finally {
            buffer.release();
        }
    }

    @Test
    public void rejectsInvalidTargetsAndAnimationKeys() {
        assertFalse(new StatelessEntityPlayAnimPacket(-1, false, ANIMATION).isValid());
        assertFalse(new StatelessEntityStopAnimPacket(1, false, "").isValid());
        assertFalse(new StatelessBlockEntityStopAnimPacket(0, 0, 0, "").isValid());
        assertFalse(new StatelessSingletonPlayAnimPacket("", 1, ANIMATION).isValid());
        assertFalse(new StatelessSingletonPlayAnimPacket("example.Item0", 0, ANIMATION).isValid());
        assertFalse(new StatelessSingletonStopAnimPacket("example.Item0", 1, "").isValid());
    }

    @Test
    public void stopPacketsAcceptBoundedMultiStageKeysLongerThanTriggerNames() {
        StringBuilder firstName = new StringBuilder("animation.test.");
        StringBuilder secondName = new StringBuilder("animation.test.");

        while (firstName.length() < 200) firstName.append('a');
        while (secondName.length() < 200) secondName.append('b');

        String animationKey = StatelessAnimatable.animationKey(
            RawAnimation.begin()
                .thenPlay(firstName.toString())
                .thenLoop(secondName.toString()));
        ByteBuf buffer = Unpooled.buffer();

        try {
            StatelessEntityStopAnimPacket written = new StatelessEntityStopAnimPacket(9, false, animationKey);
            StatelessEntityStopAnimPacket read = new StatelessEntityStopAnimPacket();

            written.toBytes(buffer);
            read.fromBytes(buffer);

            assertTrue(animationKey.length() > 256);
            assertTrue(read.isValid());
            assertEquals(animationKey, read.animation());
        } finally {
            buffer.release();
        }
    }
}
