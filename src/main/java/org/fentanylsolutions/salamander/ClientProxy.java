package org.fentanylsolutions.salamander;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;

import com.geckolib.client.resource.GeckoLibResourceReloadListener;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        if (!(Minecraft.getMinecraft()
            .getResourceManager() instanceof IReloadableResourceManager))
            throw new IllegalStateException("Minecraft resource manager is not reloadable");

        ((IReloadableResourceManager) Minecraft.getMinecraft()
            .getResourceManager()).registerReloadListener(GeckoLibResourceReloadListener.INSTANCE);
    }
}
