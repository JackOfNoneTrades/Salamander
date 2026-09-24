package org.fentanylsolutions.salamander.mixins.early.minecraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

import org.fentanylsolutions.salamander.cem.client.CemRuntime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelRenderer.class)
public abstract class MixinModelRendererCem {

    @Shadow
    private ModelBase baseModel;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void salamander$render(float scale, CallbackInfo ci) {
        if (CemRuntime.part(baseModel, (ModelRenderer) (Object) this, scale, false)) ci.cancel();
    }

    @Inject(method = "renderWithRotation", at = @At("HEAD"), cancellable = true)
    private void salamander$rotated(float scale, CallbackInfo ci) {
        if (CemRuntime.part(baseModel, (ModelRenderer) (Object) this, scale, true)) ci.cancel();
    }

    @Inject(method = "postRender", at = @At("HEAD"), cancellable = true)
    private void salamander$post(float scale, CallbackInfo ci) {
        if (CemRuntime.post(baseModel, (ModelRenderer) (Object) this, scale)) ci.cancel();
    }
}
