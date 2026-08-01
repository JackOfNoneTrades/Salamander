package com.geckolib.constant;

/** Forge 1.7 humanoid armor slots in render-pass order. */
public enum ArmorRenderSlot {

    HEAD(0),
    CHEST(1),
    LEGS(2),
    FEET(3);

    private final int armorSlot;

    ArmorRenderSlot(int armorSlot) {
        this.armorSlot = armorSlot;
    }

    public int armorSlot() {
        return this.armorSlot;
    }

    public static ArmorRenderSlot fromArmorSlot(int armorSlot) {
        switch (armorSlot) {
            case 0:
                return HEAD;
            case 1:
                return CHEST;
            case 2:
                return LEGS;
            case 3:
                return FEET;
            default:
                throw new IllegalArgumentException("Invalid armor slot: " + armorSlot);
        }
    }
}
