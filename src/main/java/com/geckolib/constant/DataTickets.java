package com.geckolib.constant;

import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.constant.dataticket.DataTicket;
import com.google.common.reflect.TypeToken;

/** Common data tickets that are independent of the client renderer. */
public final class DataTickets {

    public static final DataTicket<Long> ANIMATABLE_INSTANCE_ID = DataTicket
        .create("animatable_instance_id", Long.class);
    public static final DataTicket<Float> PARTIAL_TICK = DataTicket.create("partial_tick", Float.class);
    public static final DataTicket<Double> TICK = DataTicket.create("tick", Double.class);
    public static final DataTicket<Boolean> IS_MOVING = DataTicket.create("is_moving", Boolean.class);
    public static final DataTicket<AnimatableManager<? extends GeoAnimatable>> ANIMATABLE_MANAGER = DataTicket
        .create("animatable_manager", new TypeToken<AnimatableManager<? extends GeoAnimatable>>() {});

    private DataTickets() {}

    public static <D> DataTicket<D> create(String id, Class<? extends D> objectType) {
        return DataTicket.create(id, objectType);
    }

    public static <D> DataTicket<D> create(String id, TypeToken<? extends D> typeToken) {
        return DataTicket.create(id, typeToken);
    }
}
