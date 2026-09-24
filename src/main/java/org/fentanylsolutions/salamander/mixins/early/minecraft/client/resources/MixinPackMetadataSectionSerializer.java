package org.fentanylsolutions.salamander.mixins.early.minecraft.client.resources;

import net.minecraft.client.resources.data.PackMetadataSectionSerializer;

import org.fentanylsolutions.salamander.cem.loading.CemPackMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.google.gson.JsonElement;

@Mixin(PackMetadataSectionSerializer.class)
public abstract class MixinPackMetadataSectionSerializer {

    @ModifyVariable(
        method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/resources/data/PackMetadataSection;",
        at = @At("HEAD"),
        argsOnly = true)
    private JsonElement salamander$modernPackMetadata(JsonElement element) {
        return CemPackMetadata.legacyCompatible(element);
    }
}
