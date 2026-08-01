package org.fentanylsolutions.salamander.mixins.early.minecraft.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;

import org.fentanylsolutions.salamander.mixins.early.minecraft.client.AccessorMinecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.geckolib.renderer.GeoArmorRenderer;

/** Adds registered GeckoLib chest-armor geometry after vanilla renders a first-person arm. */
@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {

    @Inject(method = "renderFirstPersonArm", at = @At("TAIL"))
    private void salamander$renderFirstPersonArmor(EntityPlayer player, CallbackInfo callbackInfo) {
        Minecraft minecraft = Minecraft.getMinecraft();

        if (player != minecraft.thePlayer) return;

        GeoArmorRenderer
            .renderFirstPersonArm(player, ((AccessorMinecraft) minecraft).salamander$getTimer().renderPartialTicks);
    }
}
