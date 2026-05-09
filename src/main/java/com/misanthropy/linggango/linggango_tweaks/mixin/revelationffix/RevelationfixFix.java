package com.misanthropy.linggango.linggango_tweaks.mixin.revelationffix;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

@SuppressWarnings("unused")
@Pseudo
@Mixin(targets = "com.mega.revelationfix.common.enchantment.RealityPiercerEnchantment", remap = false)
public abstract class RevelationfixFix extends Enchantment {

    protected RevelationfixFix(Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slots) {
        super(rarity, category, slots);
    }

    /**
     * @author Misanthropy
     * @reason Replaces hardcoded path with a safer one. Because fuck hardcoded stuff
     */
    @Overwrite
    public void m_7677_(LivingEntity attacker, Entity target, int level) {
        if (target == null) return;
        String entityName = target.getType().toString();
        boolean isImmortal = entityName.equals("entity.eeeabsmobs.immortal") || entityName.contains("immortal");

        if (isImmortal) {
            LivingEntity livingTarget = (LivingEntity) target;
        }
    }
}