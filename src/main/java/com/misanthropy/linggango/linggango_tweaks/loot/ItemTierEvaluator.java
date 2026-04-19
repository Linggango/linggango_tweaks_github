package com.misanthropy.linggango.linggango_tweaks.loot;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

public class ItemTierEvaluator {

    public static boolean isEquipmentOP(Item item) {
        if (item instanceof SwordItem || item instanceof DiggerItem || item instanceof TridentItem) {
            double damage = 1.0 + getAttributeValue(item, Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND);
            double speed = 4.0 + getAttributeValue(item, Attributes.ATTACK_SPEED, EquipmentSlot.MAINHAND);
            double dps = damage * Math.max(0.1, speed);

            if (dps > 14.5) {
                return true;
            }
        }

        if (item instanceof ArmorItem armor) {
            EquipmentSlot slot = armor.getEquipmentSlot();
            int netheriteDef = 0;

            if (slot == EquipmentSlot.HEAD) netheriteDef = 3;
            else if (slot == EquipmentSlot.CHEST) netheriteDef = 8;
            else if (slot == EquipmentSlot.LEGS) netheriteDef = 6;
            else if (slot == EquipmentSlot.FEET) netheriteDef = 3;

            double def = getAttributeValue(item, Attributes.ARMOR, slot);
            double toughness = getAttributeValue(item, Attributes.ARMOR_TOUGHNESS, slot);

            return def > netheriteDef || toughness > 3.0;
        }

        return false;
    }

    public static Rarity evaluateRarity(Item item) {
        if (item instanceof SwordItem || item instanceof DiggerItem || item instanceof TridentItem) {
            double damage = 1.0 + getAttributeValue(item, Attributes.ATTACK_DAMAGE, EquipmentSlot.MAINHAND);
            double speed = 4.0 + getAttributeValue(item, Attributes.ATTACK_SPEED, EquipmentSlot.MAINHAND);
            double dps = damage * Math.max(0.1, speed);

            if (dps >= 11.5) return Rarity.EPIC;
            if (dps >= 8.0) return Rarity.UNCOMMON;
            return Rarity.COMMON;
        }

        if (item instanceof ArmorItem armor) {
            EquipmentSlot slot = armor.getEquipmentSlot();
            double def = getAttributeValue(item, Attributes.ARMOR, slot);

            if (def >= 6.0) return Rarity.EPIC;
            if (def >= 3.0) return Rarity.UNCOMMON;
            return Rarity.COMMON;
        }

        return item.getDefaultInstance().getRarity();
    }

    public static boolean hasTooManyBuffs(Item item) {
        int buffCount = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            item.getDefaultAttributeModifiers(slot);
            buffCount += item.getDefaultAttributeModifiers(slot).size();
        }
        return buffCount > 5;
    }

    private static double getAttributeValue(Item item, Attribute attribute, EquipmentSlot slot) {
        double total = 0;
        item.getDefaultAttributeModifiers(slot);
        if (item.getDefaultAttributeModifiers(slot).containsKey(attribute)) {
            for (AttributeModifier mod : item.getDefaultAttributeModifiers(slot).get(attribute)) {
                total += mod.getAmount();
            }
        }
        return total;
    }
}