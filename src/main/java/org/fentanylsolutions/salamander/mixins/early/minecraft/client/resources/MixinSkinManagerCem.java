package org.fentanylsolutions.salamander.mixins.early.minecraft.client.resources;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.salamander.cem.client.CemPlayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

@Mixin(SkinManager.class)
public abstract class MixinSkinManagerCem {

    @Inject(method = "func_152789_a", at = @At("RETURN"))
    private void salamander$skinModel(MinecraftProfileTexture texture, MinecraftProfileTexture.Type type,
        SkinManager.SkinAvailableCallback callback, CallbackInfoReturnable<ResourceLocation> ci) {
        if (type == MinecraftProfileTexture.Type.SKIN && callback instanceof AbstractClientPlayer)
            CemPlayers.skinModel((AbstractClientPlayer) callback, "slim".equals(texture.getMetadata("model")));
    }
}
