package com.misanthropy.linggango.linggango_tweaks.enchant;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class ExtraProtectionEnchantment extends Enchantment {

    public final ProtectionType type;

    public ExtraProtectionEnchantment(@NonNull Rarity rarity, ProtectionType type) {
        super(rarity, EnchantmentCategory.ARMOR, new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        });
        this.type = type;
    }

    @Override
    public int getMinCost(int level) {
        return this.type.getBaseCost() + (level - 1) * this.type.getLevelCost();
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMinCost(level) + this.type.getLevelCost();
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public int getDamageProtection(int level, @NonNull DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return 0;

        return switch (this.type) {
            case ALL -> level;
            case FIRE -> source.is(DamageTypeTags.IS_FIRE) ? level * 2 : 0;
            case EXPLOSION -> source.is(DamageTypeTags.IS_EXPLOSION) ? level * 2 : 0;
            case PROJECTILE -> source.is(DamageTypeTags.IS_PROJECTILE) ? level * 2 : 0;
        };
    }

    @Override
    protected boolean checkCompatibility(@NotNull Enchantment otherEnchantment) {
        if (otherEnchantment instanceof ProtectionEnchantment vanillaProt) {
            if (this.type == ProtectionType.ALL && vanillaProt.type == ProtectionEnchantment.Type.ALL) return false;
            if (this.type == ProtectionType.FIRE && vanillaProt.type == ProtectionEnchantment.Type.FIRE) return false;
            if (this.type == ProtectionType.EXPLOSION && vanillaProt.type == ProtectionEnchantment.Type.EXPLOSION) return false;
            if (this.type == ProtectionType.PROJECTILE && vanillaProt.type == ProtectionEnchantment.Type.PROJECTILE) return false;
        }

        if (otherEnchantment instanceof ExtraProtectionEnchantment) {
            return false;
        }

        return super.checkCompatibility(otherEnchantment);
    }

    public enum ProtectionType {
        ALL(1, 11),
        FIRE(10, 8),
        EXPLOSION(5, 8),
        PROJECTILE(3, 6);

        private final int baseCost;
        private final int levelCost;

        ProtectionType(int baseCost, int levelCost) {
            this.baseCost = baseCost;
            this.levelCost = levelCost;
        }

        public int getBaseCost() { return baseCost; }
        public int getLevelCost() { return levelCost; }
    }
}