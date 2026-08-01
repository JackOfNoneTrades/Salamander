package com.geckolib.animatable;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.geckolib.GeckoLibConstants;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.network.GeckoLibNetwork;

/** GeckoLib animatable contract for Minecraft 1.7 tile entities. */
public interface GeoBlockEntity extends GeoAnimatable {

    /** Trigger a registered animation locally or on clients tracking this tile's chunk. */
    default void triggerAnim(String controllerName, String animationName) {
        TileEntity tileEntity = (TileEntity) this;
        World world = tileEntity.getWorldObj();

        if (world == null) {
            GeckoLibConstants.LOGGER.error(
                "Attempted to trigger animation for an unplaced block entity ({})",
                tileEntity.getClass()
                    .getName());
            return;
        }

        if (!world.isRemote) {
            GeckoLibNetwork.triggerBlockEntityAnimation(tileEntity, controllerName, animationName);
            return;
        }

        AnimatableManager<GeoBlockEntity> manager = getAnimatableInstanceCache().getManagerForId(0);

        if (controllerName == null) manager.tryTriggerAnimation(animationName);
        else manager.tryTriggerAnimation(controllerName, animationName);
    }

    /** Stop a registered triggered animation locally or on clients tracking this tile's chunk. */
    default void stopTriggeredAnim(String controllerName, String animationName) {
        TileEntity tileEntity = (TileEntity) this;
        World world = tileEntity.getWorldObj();

        if (world == null) {
            GeckoLibConstants.LOGGER.error(
                "Attempted to stop animation for an unplaced block entity ({})",
                tileEntity.getClass()
                    .getName());
            return;
        }

        if (!world.isRemote) {
            GeckoLibNetwork.stopTriggeredBlockEntityAnimation(tileEntity, controllerName, animationName);
            return;
        }

        AnimatableManager<GeoBlockEntity> manager = getAnimatableInstanceCache().getManagerForId(0);

        if (controllerName == null) manager.stopTriggeredAnimation(animationName);
        else manager.stopTriggeredAnimation(controllerName, animationName);
    }
}
