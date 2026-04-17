package com.misanthropy.linggango.linggango_tweaks.enchant;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ExplosiveRoundsEnchantment extends Enchantment {
    private static final EnchantmentCategory ENCHANTMENT_CATEGORY = EnchantmentCategory.create("linggango_gun_enchants", (item) -> {
        ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);
        return registryName != null && registryName.getNamespace().equals("terramity");
    });

    public ExplosiveRoundsEnchantment() {
        super(Rarity.RARE, ENCHANTMENT_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 10 + level * 5;
    }

    @Override
    public int getMaxCost(int level) {
        return 50;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }
}