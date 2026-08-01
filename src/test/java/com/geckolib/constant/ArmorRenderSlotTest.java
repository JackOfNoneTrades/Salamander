package com.geckolib.constant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

public class ArmorRenderSlotTest {

    @Test
    public void mapsForgeArmorPassOrder() {
        assertEquals(ArmorRenderSlot.HEAD, ArmorRenderSlot.fromArmorSlot(0));
        assertEquals(ArmorRenderSlot.CHEST, ArmorRenderSlot.fromArmorSlot(1));
        assertEquals(ArmorRenderSlot.LEGS, ArmorRenderSlot.fromArmorSlot(2));
        assertEquals(ArmorRenderSlot.FEET, ArmorRenderSlot.fromArmorSlot(3));
    }

    @Test
    public void rejectsNonArmorSlots() {
        assertThrows(IllegalArgumentException.class, () -> ArmorRenderSlot.fromArmorSlot(-1));
        assertThrows(IllegalArgumentException.class, () -> ArmorRenderSlot.fromArmorSlot(4));
    }
}
