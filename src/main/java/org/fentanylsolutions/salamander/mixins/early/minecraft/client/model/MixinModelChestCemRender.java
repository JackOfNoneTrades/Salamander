package org.fentanylsolutions.salamander.mixins.early.minecraft.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelChest;

import org.fentanylsolutions.salamander.cem.client.CemRuntime;
import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(ModelChest.class)
public abstract class MixinModelChestCemRender {

    @WrapMethod(method = "renderAll")
    private void salamander$render(Operation<Void> original) {
        if (org.fentanylsolutions.salamander.cem.client.CemChestHalves.render((ModelChest) (Object) this)) return;
        if (org.fentanylsolutions.salamander.cem.client.CemChests.render((ModelChest) (Object) this)) return;
        CemRuntime.Draw previous = CemRuntime.begin((ModelBase) (Object) this, null, 0, 0, 0, 0, 0);
        try {
            original.call();
        } finally {
            CemRuntime.end(previous, 0.0625F);
        }
    }
}
