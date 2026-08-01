package org.fentanylsolutions.salamander;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.GeoReplacedEntity;
import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animatable.stateless.StatelessAnimatable;
import com.geckolib.animatable.stateless.StatelessGeoBlockEntity;
import com.geckolib.animatable.stateless.StatelessGeoSingletonAnimatable;
import com.geckolib.animation.RawAnimation;
import com.geckolib.cache.SyncedSingletonAnimatableCache;
import com.geckolib.client.resource.GeckoLibResourceReloadListener;
import com.geckolib.renderer.GeoReplacedEntityRenderer;

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
    public void handleEntityAnimationTrigger(int entityId, boolean replacedEntity, String controllerName,
        String animationName) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                Entity entity = getClientEntity(entityId);

                if (replacedEntity) {
                    GeoReplacedEntity animatable = GeoReplacedEntityRenderer.getReplacedAnimatable(entity);

                    if (animatable != null) animatable.triggerAnim(entity, controllerName, animationName);
                } else if (entity instanceof GeoEntity) {
                    ((GeoEntity) entity).triggerAnim(controllerName, animationName);
                }
            });
    }

    @Override
    public void handleStopTriggeredEntityAnimation(int entityId, boolean replacedEntity, String controllerName,
        String animationName) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                Entity entity = getClientEntity(entityId);

                if (replacedEntity) {
                    GeoReplacedEntity animatable = GeoReplacedEntityRenderer.getReplacedAnimatable(entity);

                    if (animatable != null) animatable.stopTriggeredAnim(entity, controllerName, animationName);
                } else if (entity instanceof GeoEntity) {
                    ((GeoEntity) entity).stopTriggeredAnim(controllerName, animationName);
                }
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

    @Override
    public void handleBlockEntityAnimationTrigger(int x, int y, int z, String controllerName, String animationName) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                TileEntity tileEntity = getClientBlockEntity(x, y, z);

                if (tileEntity instanceof GeoBlockEntity)
                    ((GeoBlockEntity) tileEntity).triggerAnim(controllerName, animationName);
            });
    }

    @Override
    public void handleStopTriggeredBlockEntityAnimation(int x, int y, int z, String controllerName,
        String animationName) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                TileEntity tileEntity = getClientBlockEntity(x, y, z);

                if (tileEntity instanceof GeoBlockEntity)
                    ((GeoBlockEntity) tileEntity).stopTriggeredAnim(controllerName, animationName);
            });
    }

    @Override
    public void handleStatelessEntityAnimationPlay(int entityId, boolean replacedEntity, RawAnimation animation) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                Entity entity = getClientEntity(entityId);
                GeoAnimatable animatable = replacedEntity ? GeoReplacedEntityRenderer.getReplacedAnimatable(entity)
                    : entity instanceof GeoAnimatable ? (GeoAnimatable) entity : null;

                if (animatable instanceof StatelessAnimatable)
                    ((StatelessAnimatable) animatable).handleClientAnimationPlay(animatable, entityId, animation);
            });
    }

    @Override
    public void handleStatelessEntityAnimationStop(int entityId, boolean replacedEntity, String animation) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                Entity entity = getClientEntity(entityId);
                GeoAnimatable animatable = replacedEntity ? GeoReplacedEntityRenderer.getReplacedAnimatable(entity)
                    : entity instanceof GeoAnimatable ? (GeoAnimatable) entity : null;

                if (animatable instanceof StatelessAnimatable)
                    ((StatelessAnimatable) animatable).handleClientAnimationStop(animatable, entityId, animation);
            });
    }

    @Override
    public void handleStatelessSingletonAnimationPlay(String syncableId, long instanceId, RawAnimation animation) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                SingletonGeoAnimatable animatable = SyncedSingletonAnimatableCache.getSyncedAnimatable(syncableId);

                if (animatable instanceof StatelessGeoSingletonAnimatable)
                    ((StatelessGeoSingletonAnimatable) animatable)
                        .handleClientAnimationPlay(animatable, instanceId, animation);
            });
    }

    @Override
    public void handleStatelessSingletonAnimationStop(String syncableId, long instanceId, String animation) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                SingletonGeoAnimatable animatable = SyncedSingletonAnimatableCache.getSyncedAnimatable(syncableId);

                if (animatable instanceof StatelessGeoSingletonAnimatable)
                    ((StatelessGeoSingletonAnimatable) animatable)
                        .handleClientAnimationStop(animatable, instanceId, animation);
            });
    }

    @Override
    public void handleStatelessBlockEntityAnimationPlay(int x, int y, int z, RawAnimation animation) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                TileEntity tileEntity = getClientBlockEntity(x, y, z);

                if (tileEntity instanceof StatelessGeoBlockEntity) ((StatelessGeoBlockEntity) tileEntity)
                    .handleClientAnimationPlay((GeoAnimatable) tileEntity, 0, animation);
            });
    }

    @Override
    public void handleStatelessBlockEntityAnimationStop(int x, int y, int z, String animation) {
        Minecraft.getMinecraft()
            .func_152344_a(() -> {
                TileEntity tileEntity = getClientBlockEntity(x, y, z);

                if (tileEntity instanceof StatelessGeoBlockEntity) ((StatelessGeoBlockEntity) tileEntity)
                    .handleClientAnimationStop((GeoAnimatable) tileEntity, 0, animation);
            });
    }

    private static Entity getClientEntity(int entityId) {
        return Minecraft.getMinecraft().theWorld == null ? null
            : Minecraft.getMinecraft().theWorld.getEntityByID(entityId);
    }

    private static TileEntity getClientBlockEntity(int x, int y, int z) {
        return Minecraft.getMinecraft().theWorld == null ? null
            : Minecraft.getMinecraft().theWorld.getTileEntity(x, y, z);
    }
}
