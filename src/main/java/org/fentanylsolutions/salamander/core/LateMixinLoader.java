package org.fentanylsolutions.salamander.core;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.fentanylsolutions.salamander.Salamander;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@SuppressWarnings("unused")
@LateMixin
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class LateMixinLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins." + Salamander.MODID + ".late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        return Collections.emptyList();
    }
}
