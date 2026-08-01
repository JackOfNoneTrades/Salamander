package com.geckolib.animatable.stateless;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.geckolib.GeckoLibConstants;
import com.geckolib.animatable.GeoBlockEntity;
import com.geckolib.animation.RawAnimation;
import com.geckolib.network.GeckoLibNetwork;

/** Stateless animation convenience API for Minecraft 1.7 tile entities. */
public interface StatelessGeoBlockEntity extends StatelessAnimatable, GeoBlockEntity {

    @Override
    default void playAnimation(RawAnimation animation) {
        TileEntity tileEntity = (TileEntity) this;
        World world = tileEntity.getWorldObj();

        if (world == null) {
            GeckoLibConstants.LOGGER.error(
                "Attempted to play animation for an unplaced block entity ({})",
                tileEntity.getClass()
                    .getName());
            return;
        }

        if (world.isRemote) handleClientAnimationPlay(this, 0, animation);
        else GeckoLibNetwork.playStatelessBlockEntityAnimation(tileEntity, animation);
    }

    @Override
    default void stopAnimation(String animation) {
        TileEntity tileEntity = (TileEntity) this;
        World world = tileEntity.getWorldObj();

        if (world == null) {
            GeckoLibConstants.LOGGER.error(
                "Attempted to stop animation for an unplaced block entity ({})",
                tileEntity.getClass()
                    .getName());
            return;
        }

        if (world.isRemote) handleClientAnimationStop(this, 0, animation);
        else GeckoLibNetwork.stopStatelessBlockEntityAnimation(tileEntity, animation);
    }
}
