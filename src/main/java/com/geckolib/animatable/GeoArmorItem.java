package com.geckolib.animatable;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.util.GeckoLibUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Convenient Forge 1.7 armor base wired to a registered {@link GeoArmorRenderer}. */
public abstract class GeoArmorItem extends ItemArmor implements GeoItem {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected GeoArmorItem(ArmorMaterial material, int renderIndex, int armorSlot) {
        super(material, renderIndex, armorSlot);
    }

    @Override
    public final AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, int armorSlot) {
        GeoArmorRenderer<?> renderer = GeoArmorRenderer.getArmorRenderer(this);

        if (renderer == null) return null;

        renderer.prepareForRender(entity, stack, armorSlot);

        return renderer;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getArmorTexture(ItemStack stack, Entity entity, int armorSlot, String type) {
        GeoArmorRenderer<?> renderer = GeoArmorRenderer.getArmorRenderer(this);

        return renderer == null ? null
            : renderer.getTextureResource(stack)
                .toString();
    }
}
