package org.fentanylsolutions.salamander.mixins.early.minecraft.client.renderer;

import java.awt.image.BufferedImage;

import net.minecraft.client.renderer.ImageBufferDownload;

import org.fentanylsolutions.salamander.cem.client.CemSkinImage;
import org.fentanylsolutions.salamander.cem.client.CemSkinImages;
import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

@Mixin(ImageBufferDownload.class)
public abstract class MixinImageBufferDownloadCem {

    @WrapMethod(method = "parseUserSkin")
    private BufferedImage salamander$preserveSkin(BufferedImage image, Operation<BufferedImage> original) {
        BufferedImage preserved = CemSkinImage.copy(image);
        BufferedImage processed = original.call(image);
        CemSkinImages.remember(processed, preserved);
        return processed;
    }
}
