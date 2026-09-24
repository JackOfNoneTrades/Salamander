package org.fentanylsolutions.salamander.mixins.early.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;

import org.fentanylsolutions.salamander.cem.network.CemNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityMob.class)
public abstract class MixinEntityMobCem {

    @Inject(method = "attackEntityAsMob", at = @At("HEAD"))
    private void salamander$observeAttack(Entity target, CallbackInfoReturnable<Boolean> cir) {
        CemNetwork.attacked((Entity) (Object) this);
    }
}
