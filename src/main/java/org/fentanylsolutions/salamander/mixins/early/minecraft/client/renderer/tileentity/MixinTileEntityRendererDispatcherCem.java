package org.fentanylsolutions.salamander.mixins.early.minecraft.client.renderer.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

import org.fentanylsolutions.salamander.cem.client.CemRuntime;
import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(TileEntityRendererDispatcher.class)
public abstract class MixinTileEntityRendererDispatcherCem {

    @WrapMethod(method = "renderTileEntityAt")
    private void salamander$scope(TileEntity tile, double x, double y, double z, float partial,
        Operation<Void> original) {
        CemRuntime.Subject previous = CemRuntime.enter(tile, partial);
        try {
            original.call(tile, x, y, z, partial);
        } finally {
            CemRuntime.leave(previous);
        }
    }
}
