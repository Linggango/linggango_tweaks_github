package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SynergiesArmorTweaks {
    private static final Set<String> SPECIAL_BUFF_ARMOR = Set.of(
            "elemental_synergies:neru_helmet",
            "elemental_synergies:neru_chestplate",
            "elemental_synergies:neru_leggings",
            "elemental_synergies:neru_boots",

            "elemental_synergies:project_sekai_helmet",
            "elemental_synergies:project_sekai_chestplate",
            "elemental_synergies:project_sekai_leggings",
            "elemental_synergies:project_sekai_boots",

            "elemental_synergies:rotten_girl_helmet",
            "elemental_synergies:rotten_girl_chestplate",
            "elemental_synergies:rotten_girl_leggings",
            "elemental_synergies:rotten_girl_boots",

            "elemental_synergies:utau_helmet",
            "elemental_synergies:utau_chestplate",
            "elemental_synergies:utau_leggings",
            "elemental_synergies:utau_boots",

            "elemental_synergies:synthesizer_v_helmet",
            "elemental_synergies:synthesizer_v_chestplate",
            "elemental_synergies:synthesizer_v_leggings",
            "elemental_synergies:synthesizer_v_boots"
    );

    @SubscribeEvent
    public static void onArmorAttributes(ItemAttributeModifierEvent event) {
        if (event.getItemStack().getItem() instanceof ArmorItem armorItem) {
            if (event.getSlotType() == armorItem.getEquipmentSlot()) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(armorItem);

                if (id != null) {
                    double bonusArmor = 4.0;
                    double bonusToughness = 2.0;
                    if (SPECIAL_BUFF_ARMOR.contains(id.toString())) {
                        bonusArmor = 6.0;
                        bonusToughness = 3.0;
                    }

                    UUID armorUUID = UUID.nameUUIDFromBytes(("linggango_armor_buff_" + event.getSlotType().getName()).getBytes());
                    UUID toughnessUUID = UUID.nameUUIDFromBytes(("linggango_toughness_buff_" + event.getSlotType().getName()).getBytes());
                    event.addModifier(Attributes.ARMOR, new AttributeModifier(armorUUID, "Linggango Armor Buff", bonusArmor, AttributeModifier.Operation.ADDITION));
                    event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(toughnessUUID, "Linggango Toughness Buff", bonusToughness, AttributeModifier.Operation.ADDITION));
                }
            }
        }
    }
}