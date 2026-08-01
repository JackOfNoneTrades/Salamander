package com.geckolib.cache;

import static org.junit.Assert.assertEquals;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.Test;

public class AnimatableIdCacheTest {

    @Test
    public void persistentCounterRoundTripsAndRejectsNegativeValues() {
        AnimatableIdCache cache = new AnimatableIdCache();
        NBTTagCompound stored = new NBTTagCompound();
        NBTTagCompound restored = new NBTTagCompound();

        stored.setLong("last_id", 73);
        cache.readFromNBT(stored);
        cache.writeToNBT(restored);

        assertEquals(73, restored.getLong("last_id"));

        stored.setLong("last_id", -4);
        cache.readFromNBT(stored);
        cache.writeToNBT(restored);

        assertEquals(0, restored.getLong("last_id"));
    }
}
