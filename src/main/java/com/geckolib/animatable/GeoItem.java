package com.geckolib.animatable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;

import com.geckolib.cache.AnimatableIdCache;

/** GeckoLib animatable contract for Minecraft items. */
public interface GeoItem extends SingletonGeoAnimatable {

    String ANIMATABLE_ID_NBT_KEY = "salamander:animatable_id";

    static void registerSyncedAnimatable(SingletonGeoAnimatable animatable) {
        SingletonGeoAnimatable.registerSyncedAnimatable(animatable);
    }

    /**
     * Returns an assigned persistent ID, or a stable in-memory ID without creating stack NBT.
     */
    static long getId(ItemStack stack) {
        if (stack == null) throw new IllegalArgumentException("Item stack cannot be null");

        NBTTagCompound tag = stack.getTagCompound();

        if (tag != null && tag.hasKey(ANIMATABLE_ID_NBT_KEY, NBT.TAG_LONG)) {
            long assignedId = tag.getLong(ANIMATABLE_ID_NBT_KEY);

            if (assignedId > 0) return assignedId;
        }

        return Long.MIN_VALUE | Integer.toUnsignedLong(System.identityHashCode(stack));
    }

    /**
     * Returns the armor-renderer namespace for a stack ID, kept separate from ordinary item render instances.
     */
    static long getArmorId(ItemStack stack) {
        long itemId = getId(stack);

        return itemId == Long.MIN_VALUE ? Long.MAX_VALUE : -itemId;
    }

    /** Reserves and stores an ID on first use. Must only be called from the logical server. */
    static long getOrAssignId(ItemStack stack, World world) {
        if (stack == null) throw new IllegalArgumentException("Item stack cannot be null");
        if (world == null || world.isRemote)
            throw new IllegalArgumentException("Item animation IDs can only be assigned on the server");

        NBTTagCompound tag = stack.getTagCompound();

        if (tag != null && tag.hasKey(ANIMATABLE_ID_NBT_KEY, NBT.TAG_LONG)) {
            long assignedId = tag.getLong(ANIMATABLE_ID_NBT_KEY);

            if (assignedId > 0) return assignedId;
        }

        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        long assignedId = AnimatableIdCache.getFreeId(world);

        tag.setLong(ANIMATABLE_ID_NBT_KEY, assignedId);

        return assignedId;
    }
}
