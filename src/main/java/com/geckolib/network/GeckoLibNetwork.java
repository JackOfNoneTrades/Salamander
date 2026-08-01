package com.geckolib.network;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.world.WorldServer;

import org.fentanylsolutions.salamander.Salamander;

import com.geckolib.network.packet.entity.EntityAnimTriggerPacket;
import com.geckolib.network.packet.entity.StopTriggeredEntityAnimPacket;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/** Transient server-to-client animation synchronization. */
public final class GeckoLibNetwork {

    public static final int PROTOCOL_VERSION = 1;

    private static final int ENTITY_TRIGGER_PACKET_ID = 0;
    private static final int STOP_ENTITY_TRIGGER_PACKET_ID = 1;
    private static final SimpleNetworkWrapper CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(Salamander.MODID);

    private static boolean initialized;

    private GeckoLibNetwork() {}

    public static void init() {
        if (initialized) throw new IllegalStateException("Salamander network is already initialized");

        initialized = true;
        CHANNEL.registerMessage(
            EntityAnimTriggerPacket.Handler.class,
            EntityAnimTriggerPacket.class,
            ENTITY_TRIGGER_PACKET_ID,
            Side.CLIENT);
        CHANNEL.registerMessage(
            StopTriggeredEntityAnimPacket.Handler.class,
            StopTriggeredEntityAnimPacket.class,
            STOP_ENTITY_TRIGGER_PACKET_ID,
            Side.CLIENT);
    }

    public static void triggerEntityAnimation(Entity entity, String controllerName, String animationName) {
        EntityAnimTriggerPacket message = new EntityAnimTriggerPacket(
            entity.getEntityId(),
            controllerName,
            animationName);

        if (!message.isValid()) throw new IllegalArgumentException("Invalid entity animation trigger");

        sendToTrackingAndSelf(entity, message);
    }

    public static void stopTriggeredEntityAnimation(Entity entity, String controllerName, String animationName) {
        StopTriggeredEntityAnimPacket message = new StopTriggeredEntityAnimPacket(
            entity.getEntityId(),
            controllerName,
            animationName);

        if (!message.isValid()) throw new IllegalArgumentException("Invalid entity animation stop request");

        sendToTrackingAndSelf(entity, message);
    }

    private static void sendToTrackingAndSelf(Entity entity, IMessage message) {
        if (!(entity.worldObj instanceof WorldServer)) {
            throw new IllegalArgumentException("Animation packets can only be sent for server entities");
        }

        Packet packet = CHANNEL.getPacketFrom(message);

        ((WorldServer) entity.worldObj).getEntityTracker()
            .func_151248_b(entity, packet);
    }
}
