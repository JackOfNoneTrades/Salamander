package org.fentanylsolutions.salamander.debug.client;

import org.fentanylsolutions.salamander.debug.entity.DebugCitadelDragon;
import org.fentanylsolutions.salamander.debug.entity.DebugCitadelFly;
import org.fentanylsolutions.salamander.debug.entity.DebugModelEntity;

import cpw.mods.fml.client.registry.RenderingRegistry;

public final class DebugClientContent {

    private DebugClientContent() {}

    public static void register() {
        RenderingRegistry.registerEntityRenderingHandler(DebugModelEntity.class, new DebugModelRenderer());
        RenderingRegistry.registerEntityRenderingHandler(DebugCitadelFly.class, new DebugCitadelFlyRenderer());
        RenderingRegistry.registerEntityRenderingHandler(DebugCitadelDragon.class, new DebugCitadelDragonRenderer());
    }
}
