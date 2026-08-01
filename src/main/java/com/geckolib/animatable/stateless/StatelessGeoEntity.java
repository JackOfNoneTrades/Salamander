package com.geckolib.animatable.stateless;

import net.minecraft.entity.Entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animation.RawAnimation;
import com.geckolib.network.GeckoLibNetwork;

/** Stateless animation convenience API for ordinary GeckoLib entities. */
public interface StatelessGeoEntity extends StatelessAnimatable, GeoEntity {

    @Override
    default void playAnimation(RawAnimation animation) {
        if (!(this instanceof Entity))
            throw new ClassCastException("Cannot use StatelessGeoEntity on a non-entity animatable");

        Entity entity = (Entity) this;

        if (entity.worldObj == null) return;

        if (entity.worldObj.isRemote) handleClientAnimationPlay(this, entity.getEntityId(), animation);
        else GeckoLibNetwork.playStatelessEntityAnimation(entity, false, animation);
    }

    @Override
    default void stopAnimation(String animation) {
        if (!(this instanceof Entity))
            throw new ClassCastException("Cannot use StatelessGeoEntity on a non-entity animatable");

        Entity entity = (Entity) this;

        if (entity.worldObj == null) return;

        if (entity.worldObj.isRemote) handleClientAnimationStop(this, entity.getEntityId(), animation);
        else GeckoLibNetwork.stopStatelessEntityAnimation(entity, false, animation);
    }
}
