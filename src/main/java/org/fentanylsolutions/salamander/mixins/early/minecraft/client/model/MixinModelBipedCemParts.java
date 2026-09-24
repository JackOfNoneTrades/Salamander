package org.fentanylsolutions.salamander.mixins.early.minecraft.client.model;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

import org.fentanylsolutions.salamander.cem.client.CemModelParts;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelBiped.class)
public abstract class MixinModelBipedCemParts implements CemModelParts {

    @Shadow
    private ModelRenderer bipedHead;
    @Shadow
    private ModelRenderer bipedHeadwear;
    @Shadow
    private ModelRenderer bipedBody;
    @Shadow
    private ModelRenderer bipedRightArm;
    @Shadow
    private ModelRenderer bipedLeftArm;
    @Shadow
    private ModelRenderer bipedRightLeg;
    @Shadow
    private ModelRenderer bipedLeftLeg;

    @Override
    public Map<String, ModelRenderer> salamander$cemParts() {
        Map<String, ModelRenderer> parts = new LinkedHashMap<>();
        parts.put("head", bipedHead);
        parts.put("headwear", bipedHeadwear);
        parts.put("body", bipedBody);
        parts.put("right_arm", bipedRightArm);
        parts.put("left_arm", bipedLeftArm);
        parts.put("right_leg", bipedRightLeg);
        parts.put("left_leg", bipedLeftLeg);
        return parts;
    }
}
