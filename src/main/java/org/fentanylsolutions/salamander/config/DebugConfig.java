package org.fentanylsolutions.salamander.config;

import org.fentanylsolutions.salamander.Salamander;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = Salamander.MODID, category = "debug", configSubDirectory = Salamander.MODID)
@Config.LangKeyPattern(pattern = "%mod.config.%cat.%field")
public final class DebugConfig {

    @Config.Comment("Register Salamander's development-only model test entities. Must match on client and server.")
    @Config.DefaultBoolean(false)
    @Config.RequiresMcRestart
    public static boolean debugMode = false;

    private DebugConfig() {}
}
