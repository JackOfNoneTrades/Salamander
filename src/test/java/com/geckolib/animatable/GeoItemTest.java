package com.geckolib.animatable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.Test;

import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.cache.SyncedSingletonAnimatableCache;
import com.geckolib.util.GeckoLibUtil;

public class GeoItemTest {

    @Test
    public void transientIdentityDoesNotCreateStackNbt() {
        ItemStack first = new ItemStack(new Item());
        ItemStack second = new ItemStack(new Item());
        long firstId = GeoItem.getId(first);

        assertFalse(first.hasTagCompound());
        assertTrue(firstId < 0);
        assertEquals(firstId, GeoItem.getId(first));
        assertNotEquals(firstId, GeoItem.getId(second));
        assertFalse(second.hasTagCompound());
    }

    @Test
    public void assignedPositiveIdentityTakesPrecedence() {
        ItemStack stack = new ItemStack(new Item());
        NBTTagCompound tag = new NBTTagCompound();

        tag.setLong(GeoItem.ANIMATABLE_ID_NBT_KEY, 42);
        stack.setTagCompound(tag);

        assertEquals(42, GeoItem.getId(stack));
    }

    @Test
    public void armorIdentityUsesSeparateNamespaceWithoutCreatingNbt() {
        ItemStack transientStack = new ItemStack(new Item());
        long itemId = GeoItem.getId(transientStack);

        assertEquals(-itemId, GeoItem.getArmorId(transientStack));
        assertFalse(transientStack.hasTagCompound());

        ItemStack assignedStack = new ItemStack(new Item());
        NBTTagCompound tag = new NBTTagCompound();

        tag.setLong(GeoItem.ANIMATABLE_ID_NBT_KEY, 42);
        assignedStack.setTagCompound(tag);

        assertEquals(-42, GeoItem.getArmorId(assignedStack));
    }

    @Test
    public void singletonRegistryDistinguishesInstancesOfTheSameClass() {
        TestItem first = new TestItem();
        TestItem second = new TestItem();

        GeoItem.registerSyncedAnimatable(first);
        GeoItem.registerSyncedAnimatable(second);

        String firstId = SyncedSingletonAnimatableCache.getOrCreateId(first);
        String secondId = SyncedSingletonAnimatableCache.getOrCreateId(second);

        assertNotEquals(firstId, secondId);
        assertSame(first, SyncedSingletonAnimatableCache.getSyncedAnimatable(firstId));
        assertSame(second, SyncedSingletonAnimatableCache.getSyncedAnimatable(secondId));
    }

    private static final class TestItem extends Item implements GeoItem {

        private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {}

        @Override
        public AnimatableInstanceCache getAnimatableInstanceCache() {
            return this.cache;
        }
    }
}
