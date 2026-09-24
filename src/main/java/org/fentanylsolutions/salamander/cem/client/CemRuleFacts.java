package org.fentanylsolutions.salamander.cem.client;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.fentanylsolutions.salamander.cem.loading.CemRules;
import org.fentanylsolutions.salamander.mixins.early.minecraft.client.AccessorCemNbtList;

/** A lazy, per-tick snapshot. Ordinary rendering never serializes NBT unless a rule actually asks for it. */
public final class CemRuleFacts implements CemRules.Facts {

    private static final Map<Object, CemRuleFacts> CACHE = new WeakHashMap<>();
    private static final String[] COLORS = { "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink",
        "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black" };
    private final WeakReference<Object> subject;
    private final long tick, seed;
    private final Map<String, String> values = new HashMap<>();
    private NBTTagCompound nbt;

    public static void clear() {
        CACHE.clear();
    }

    public static CemRuleFacts get(Object object) {
        long tick = object instanceof Entity ? ((Entity) object).ticksExisted
            : object instanceof TileEntity && ((TileEntity) object).getWorldObj() != null
                ? ((TileEntity) object).getWorldObj()
                    .getTotalWorldTime()
                : CemClient.frame();
        CemRuleFacts facts = CACHE.get(object);
        if (facts == null || facts.tick != tick) {
            facts = new CemRuleFacts(object, tick);
            CACHE.put(object, facts);
        }
        return facts;
    }

    private CemRuleFacts(Object object, long tick) {
        this.tick = tick;
        subject = new WeakReference<>(object);
        Entity entity = object instanceof Entity ? (Entity) object : null;
        TileEntity tile = object instanceof TileEntity ? (TileEntity) object : null;
        World world = entity != null ? entity.worldObj : tile == null ? null : tile.getWorldObj();
        int x = entity != null ? MathHelper.floor_double(entity.posX) : tile == null ? 0 : tile.xCoord;
        int y = entity != null ? MathHelper.floor_double(entity.boundingBox.minY) : tile == null ? 0 : tile.yCoord;
        int z = entity != null ? MathHelper.floor_double(entity.posZ) : tile == null ? 0 : tile.zCoord;
        seed = entity != null ? entity.getUniqueID()
            .getLeastSignificantBits() & 0x7fffffffL : (x * 73428767L ^ y * 912931L ^ z * 438289L) & 0x7fffffffL;
        values.put("heights", String.valueOf(y));
        if (world != null) {
            values.put("biomes", world.getBiomeGenForCoords(x, z).biomeName);
            values.put("moonPhase", String.valueOf(world.getMoonPhase()));
            values.put("dayTime", String.valueOf(world.getWorldTime() % 24000));
            values.put("weather", world.isThundering() ? "thunder" : world.isRaining() ? "rain" : "clear");
            int blockY = entity == null ? y : y - 1;
            Block block = world.getBlock(x, blockY, z);
            String name = String.valueOf(Block.blockRegistry.getNameForObject(block));
            values.put("blocks", name + ":" + world.getBlockMetadata(x, blockY, z));
        }
        if (tile != null && tile.getWorldObj() != null && tile.getBlockType() instanceof net.minecraft.block.BlockBed) {
            String blockName = String.valueOf(Block.blockRegistry.getNameForObject(tile.getBlockType()));
            String color = blockName.substring(blockName.indexOf(':') + 1)
                .replace("_bed", "");
            values.put("colors", color.equals("bed") ? "red" : color.equals("silver") ? "light_gray" : color);
        }
        if (object instanceof net.minecraft.inventory.IInventory) {
            net.minecraft.inventory.IInventory inventory = (net.minecraft.inventory.IInventory) object;
            if (inventory.hasCustomInventoryName()) values.put("name", inventory.getInventoryName());
        }
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            values.put("health", String.valueOf(living.getHealth()));
            values.put(
                "healthPercent",
                String.valueOf(living.getMaxHealth() > 0 ? 100 * living.getHealth() / living.getMaxHealth() : 0));
            values.put("baby", String.valueOf(living.isChild()));
            if (living instanceof EntityLiving && ((EntityLiving) living).hasCustomNameTag())
                values.put("name", ((EntityLiving) living).getCustomNameTag());
        }
        if (entity instanceof EntitySlime)
            values.put("sizes", String.valueOf(((EntitySlime) entity).getSlimeSize() - 1));
        if (entity instanceof EntitySheep) values.put("colors", COLORS[((EntitySheep) entity).getFleeceColor() & 15]);
        if (entity instanceof EntityWolf) values.put("colors", COLORS[((EntityWolf) entity).getCollarColor() & 15]);
        if (entity instanceof EntityVillager) {
            int profession = ((EntityVillager) entity).getProfession();
            String[] names = { "farmer", "librarian", "cleric", "blacksmith", "butcher" };
            if (profession >= 0 && profession < names.length) values.put("professions", names[profession]);
        }
    }

    @Override
    public String value(String key) {
        if (key.equals("colors") && !values.containsKey(key)) {
            Object object = subject.get();
            if (object != null && object.getClass()
                .getName()
                .equals("ganymedes01.etfuturum.tileentities.TileEntityShulkerBox")) {
                List<String> colors = nbt("Color", false);
                int color = colors.isEmpty() ? 0 : Integer.parseInt(colors.get(0));
                if (color > 0 && color <= 16) values.put(key, COLORS[color - 1]);
            }
        }
        return values.get(key);
    }

    @Override
    public long seed() {
        return seed;
    }

    @Override
    public List<String> nbt(String path, boolean raw) {
        if (nbt == null) {
            nbt = new NBTTagCompound();
            Object object = subject.get();
            if (object instanceof Entity) ((Entity) object).writeToNBT(nbt);
            else if (object instanceof TileEntity) ((TileEntity) object).writeToNBT(nbt);
            // Vanilla snow golems in 1.7 always have their pumpkin; later removability uses the backport's NBT.
            if (object != null && object.getClass() == net.minecraft.entity.monster.EntitySnowman.class)
                nbt.setBoolean("Pumpkin", true);
        }
        List<String> result = new ArrayList<>();
        walk(nbt, path.split("\\."), 0, raw, result);
        return result;
    }

    private static void walk(NBTBase tag, String[] path, int index, boolean raw, List<String> result) {
        if (tag == null || result.size() > 4096) return;
        if (index == path.length) {
            if (raw) result.add(tag.toString());
            else if (tag instanceof NBTTagString) result.add(((NBTTagString) tag).func_150285_a_());
            else if (tag instanceof NBTBase.NBTPrimitive) {
                double number = ((NBTBase.NBTPrimitive) tag).func_150286_g();
                result.add(number == (long) number ? String.valueOf((long) number) : String.valueOf(number));
            } else result.add(tag.toString());
            return;
        }
        String key = path[index];
        if (tag instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) tag;
            if (key.equals("*")) for (String child : compound.func_150296_c())
                walk(compound.getTag(child), path, index + 1, raw, result);
            else walk(compound.getTag(key), path, index + 1, raw, result);
        } else if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) tag;
            if (key.equals("count") && index + 1 == path.length) {
                result.add(String.valueOf(list.tagCount()));
                return;
            }
            List<NBTBase> elements = ((AccessorCemNbtList) list).salamander$elements();
            if (key.equals("*")) for (NBTBase child : elements) walk(child, path, index + 1, raw, result);
            else try {
                int at = Integer.parseInt(key);
                if (at >= 0 && at < elements.size()) walk(elements.get(at), path, index + 1, raw, result);
            } catch (NumberFormatException ignored) {}
        }
    }
}
