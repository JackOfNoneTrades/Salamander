package com.geckolib.animatable.instance;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.constant.dataticket.DataTicket;

/** Base cache for retrieving animation managers by stable instance ID. */
public abstract class AnimatableInstanceCache {

    protected final GeoAnimatable animatable;

    protected AnimatableInstanceCache(GeoAnimatable animatable) {
        this.animatable = animatable;
    }

    public abstract <T extends GeoAnimatable> AnimatableManager<T> getManagerForId(long uniqueId);

    public <D> void addDataPoint(long uniqueId, DataTicket<D> dataTicket, D data) {
        getManagerForId(uniqueId).setAnimatableData(dataTicket, data);
    }

    public <D> D getDataPoint(long uniqueId, DataTicket<D> dataTicket) {
        return getManagerForId(uniqueId).getAnimatableData(dataTicket);
    }
}
