package org.fentanylsolutions.salamander.config;

import org.fentanylsolutions.salamander.Salamander;

import com.gtnewhorizon.gtnhlib.config.Config;

@Config(modid = Salamander.MODID, category = "textures", configSubDirectory = Salamander.MODID)
public final class TextureConfig {

    @Config.Comment("Load random entity texture variants and rules from resource packs, independently of CEM. Reload resources after changing.")
    @Config.DefaultBoolean(true)
    public static boolean randomEntityTextures = true;

    private TextureConfig() {}
}
