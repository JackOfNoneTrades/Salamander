package com.geckolib;

import net.minecraft.util.ResourceLocation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Shared identifiers and logging entry points for the GeckoLib-compatible API. */
public final class GeckoLibConstants {

    public static final String MODID = "salamander";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private GeckoLibConstants() {}

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }
}
