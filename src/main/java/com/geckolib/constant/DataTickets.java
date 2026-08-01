package com.geckolib.constant;

import net.minecraft.item.ItemStack;

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
    public static final DataTicket<Float> LIMB_SWING = DataTicket.create("limb_swing", Float.class);
    public static final DataTicket<Float> LIMB_SWING_AMOUNT = DataTicket.create("limb_swing_amount", Float.class);
    public static final DataTicket<Float> NET_HEAD_YAW = DataTicket.create("net_head_yaw", Float.class);
    public static final DataTicket<Float> HEAD_PITCH = DataTicket.create("head_pitch", Float.class);
    public static final DataTicket<Boolean> IS_CHILD = DataTicket.create("is_child", Boolean.class);
    public static final DataTicket<Boolean> IS_SITTING = DataTicket.create("is_sitting", Boolean.class);
    public static final DataTicket<Integer> RENDER_COLOR = DataTicket.create("render_color", Integer.class);
    public static final DataTicket<ItemStack> ITEM_STACK = DataTicket.create("item_stack", ItemStack.class);
    public static final DataTicket<ItemRenderPerspective> ITEM_RENDER_PERSPECTIVE = DataTicket
        .create("item_render_perspective", ItemRenderPerspective.class);
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
