package org.fentanylsolutions.salamander.mixins.early.minecraft.client.renderer.texture;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import org.fentanylsolutions.salamander.cem.client.CemRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public abstract class MixinTextureManagerCem {

    @Inject(method = "bindTexture", at = @At("HEAD"))
    private void salamander$texture(ResourceLocation resource, CallbackInfo ci) {
        CemRuntime.texture(resource);
    }
}
