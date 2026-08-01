package org.fentanylsolutions.salamander;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Salamander.LOG.info("Loading {} {}", Salamander.MODNAME, Tags.VERSION);
    }
}
