package org.fentanylsolutions.salamander;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.cache.SyncedSingletonAnimatableCache;
import com.geckolib.client.resource.GeckoLibResourceReloadListener;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        if (!(Minecraft.getMinecraft()
            .getResourceManager() instanceof IReloadableResourceManager))
            throw new IllegalStateException("Minecraft resource manager is not reloadable");

        ((IReloadableResourceManager) Minecraft.getMinecraft()
            .getResourceManager()).registerReloadListener(GeckoLibResourceReloadListener.INSTANCE);
    }

    @Override
    public void handleEntityAnimationTrigger(int entityId, String controllerName, String animationName) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                Entity entity = getClientEntity(entityId);

                if (entity instanceof GeoEntity) ((GeoEntity) entity).triggerAnim(controllerName, animationName);
            });
    }

    @Override
    public void handleStopTriggeredEntityAnimation(int entityId, String controllerName, String animationName) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                Entity entity = getClientEntity(entityId);

                if (entity instanceof GeoEntity) ((GeoEntity) entity).stopTriggeredAnim(controllerName, animationName);
            });
    }

    @Override
    public void handleSingletonAnimationTrigger(String syncableId, long instanceId, String controllerName,
        String animationName) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                SingletonGeoAnimatable animatable = SyncedSingletonAnimatableCache.getSyncedAnimatable(syncableId);

                if (animatable == null) return;

                AnimatableManager<SingletonGeoAnimatable> manager = animatable.getAnimatableInstanceCache()
                    .getManagerForId(instanceId);

                if (controllerName == null) manager.tryTriggerAnimation(animationName);
                else manager.tryTriggerAnimation(controllerName, animationName);
            });
    }

    @Override
    public void handleStopTriggeredSingletonAnimation(String syncableId, long instanceId, String controllerName,
        String animationName) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                SingletonGeoAnimatable animatable = SyncedSingletonAnimatableCache.getSyncedAnimatable(syncableId);

                if (animatable == null) return;

                AnimatableManager<SingletonGeoAnimatable> manager = animatable.getAnimatableInstanceCache()
                    .getManagerForId(instanceId);

                if (controllerName == null) manager.stopTriggeredAnimation(animationName);
                else manager.stopTriggeredAnimation(controllerName, animationName);
            });
    }

    private static Entity getClientEntity(int entityId) {
        return Minecraft.getMinecraft().theWorld == null ? null
            : Minecraft.getMinecraft().theWorld.getEntityByID(entityId);
    }
}
