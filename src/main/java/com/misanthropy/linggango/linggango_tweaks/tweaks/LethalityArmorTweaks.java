package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LethalityArmorTweaks {

    private static final Set<String> BATTLE_MAID_ARMOR = Set.of(
            "lethality:battle_maid_helmet",
            "lethality:battle_maid_chestplate",
            "lethality:battle_maid_leggings",
            "lethality:battle_maid_boots"
    );

    private static final Set<String> HF_BATTLE_MAID_ARMOR = Set.of(
            "lethality:hf_battle_maid_helmet",
            "lethality:hf_battle_maid_chestplate",
            "lethality:hf_battle_maid_leggings",
            "lethality:hf_battle_maid_boots"
    );

    @SubscribeEvent
    public static void onArmorAttributes(ItemAttributeModifierEvent event) {
        Item item = event.getItemStack().getItem();

        if (item instanceof ArmorItem armorItem) {
            if (event.getSlotType() == armorItem.getEquipmentSlot()) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                if (id != null) {
                    String idStr = id.toString();
                    double bonusArmor = 0.0;
                    double bonusToughness = 0.0;
                    if (BATTLE_MAID_ARMOR.contains(idStr)) {
                        bonusArmor = 2.0;
                        bonusToughness = 2.0;
                    } else if (HF_BATTLE_MAID_ARMOR.contains(idStr)) {
                        bonusArmor = 4.0;
                        bonusToughness = 4.0;
                    }


                    if (bonusArmor > 0) {
                        UUID armorUUID = UUID.nameUUIDFromBytes(("linggango_lethality_armor_" + event.getSlotType().getName()).getBytes());
                        UUID toughnessUUID = UUID.nameUUIDFromBytes(("linggango_lethality_toughness_" + event.getSlotType().getName()).getBytes());
                        event.addModifier(Attributes.ARMOR, new AttributeModifier(armorUUID, "Lethality Armor Buff", bonusArmor, AttributeModifier.Operation.ADDITION));
                        event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(toughnessUUID, "Lethality Toughness Buff", bonusToughness, AttributeModifier.Operation.ADDITION));
                    }
                }
            }
        }
    }
}