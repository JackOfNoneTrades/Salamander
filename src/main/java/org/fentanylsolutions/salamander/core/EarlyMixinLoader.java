package org.fentanylsolutions.salamander.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fentanylsolutions.salamander.Salamander;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

@SuppressWarnings("unused")
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class EarlyMixinLoader implements IEarlyMixinLoader, IFMLLoadingPlugin {

    @Override
    public String getMixinConfig() {
        return "mixins." + Salamander.MODID + ".early.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedCoreMods) {
        return Collections.emptyList();
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
