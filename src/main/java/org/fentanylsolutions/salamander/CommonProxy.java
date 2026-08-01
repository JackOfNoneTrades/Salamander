package org.fentanylsolutions.salamander;

import com.geckolib.animation.RawAnimation;
import com.geckolib.network.GeckoLibNetwork;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Salamander.LOG.info("Loading {} {}", Salamander.MODNAME, Tags.VERSION);
        GeckoLibNetwork.init();
    }

    public void handleEntityAnimationTrigger(int entityId, boolean replacedEntity, String controllerName,
        String animationName) {}

    public void handleStopTriggeredEntityAnimation(int entityId, boolean replacedEntity, String controllerName,
        String animationName) {}

    public void handleSingletonAnimationTrigger(String syncableId, long instanceId, String controllerName,
        String animationName) {}

    public void handleStopTriggeredSingletonAnimation(String syncableId, long instanceId, String controllerName,
        String animationName) {}

    public void handleBlockEntityAnimationTrigger(int x, int y, int z, String controllerName, String animationName) {}

    public void handleStopTriggeredBlockEntityAnimation(int x, int y, int z, String controllerName,
        String animationName) {}

    public void handleStatelessEntityAnimationPlay(int entityId, boolean replacedEntity, RawAnimation animation) {}

    public void handleStatelessEntityAnimationStop(int entityId, boolean replacedEntity, String animation) {}

    public void handleStatelessSingletonAnimationPlay(String syncableId, long instanceId, RawAnimation animation) {}

    public void handleStatelessSingletonAnimationStop(String syncableId, long instanceId, String animation) {}

    public void handleStatelessBlockEntityAnimationPlay(int x, int y, int z, RawAnimation animation) {}

    public void handleStatelessBlockEntityAnimationStop(int x, int y, int z, String animation) {}

    public void handleCitadelAnimation(int entityId, int animationIndex) {}
}
