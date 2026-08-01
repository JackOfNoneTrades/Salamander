package com.geckolib.network;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.world.WorldServer;

import org.fentanylsolutions.salamander.Salamander;

import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.cache.SyncedSingletonAnimatableCache;
import com.geckolib.network.packet.entity.EntityAnimTriggerPacket;
import com.geckolib.network.packet.entity.StopTriggeredEntityAnimPacket;
import com.geckolib.network.packet.singleton.SingletonAnimTriggerPacket;
import com.geckolib.network.packet.singleton.StopTriggeredSingletonAnimPacket;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/** Transient server-to-client animation synchronization. */
public final class GeckoLibNetwork {

    public static final int PROTOCOL_VERSION = 1;

    private static final int ENTITY_TRIGGER_PACKET_ID = 0;
    private static final int STOP_ENTITY_TRIGGER_PACKET_ID = 1;
    private static final int SINGLETON_TRIGGER_PACKET_ID = 2;
    private static final int STOP_SINGLETON_TRIGGER_PACKET_ID = 3;
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
        CHANNEL.registerMessage(
            SingletonAnimTriggerPacket.Handler.class,
            SingletonAnimTriggerPacket.class,
            SINGLETON_TRIGGER_PACKET_ID,
            Side.CLIENT);
        CHANNEL.registerMessage(
            StopTriggeredSingletonAnimPacket.Handler.class,
            StopTriggeredSingletonAnimPacket.class,
            STOP_SINGLETON_TRIGGER_PACKET_ID,
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

    public static void triggerSingletonAnimation(SingletonGeoAnimatable animatable, Entity relatedEntity,
        long instanceId, String controllerName, String animationName) {
        SingletonAnimTriggerPacket message = new SingletonAnimTriggerPacket(
            SyncedSingletonAnimatableCache.getOrCreateId(animatable),
            instanceId,
            controllerName,
            animationName);

        if (!message.isValid()) throw new IllegalArgumentException("Invalid singleton animation trigger");

        sendToTrackingAndSelf(relatedEntity, message);
    }

    public static void stopTriggeredSingletonAnimation(SingletonGeoAnimatable animatable, Entity relatedEntity,
        long instanceId, String controllerName, String animationName) {
        StopTriggeredSingletonAnimPacket message = new StopTriggeredSingletonAnimPacket(
            SyncedSingletonAnimatableCache.getOrCreateId(animatable),
            instanceId,
            controllerName,
            animationName);

        if (!message.isValid()) throw new IllegalArgumentException("Invalid singleton animation stop request");

        sendToTrackingAndSelf(relatedEntity, message);
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
