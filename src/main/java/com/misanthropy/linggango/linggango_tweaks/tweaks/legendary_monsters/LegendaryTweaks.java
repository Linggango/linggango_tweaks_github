package com.misanthropy.linggango.linggango_tweaks.tweaks.legendary_monsters;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
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
public class LegendaryTweaks {

    private static final Set<String> ANNIHILATOR_ARMOR = Set.of(
            "legendary_monsters:annihilator_helmet",
            "legendary_monsters:annihilator_chestplate",
            "legendary_monsters:annihilator_leggings",
            "legendary_monsters:annihilator_boots"
    );

    @SubscribeEvent
    public static void onAttributes(ItemAttributeModifierEvent event) {
        Item item = event.getItemStack().getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id != null && id.getNamespace().equals("legendary_monsters")) {
            String path = id.getPath();
            if (event.getSlotType() == EquipmentSlot.MAINHAND) {
                double bonusDamage = getBonusDamage(path);

                UUID damageUUID = UUID.nameUUIDFromBytes(("linggango_leg_damage_" + event.getSlotType().getName()).getBytes());
                event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(damageUUID, "Legendary Damage Buff", bonusDamage, AttributeModifier.Operation.ADDITION));
            }
            if (item instanceof ArmorItem armorItem) {
                if (event.getSlotType() == armorItem.getEquipmentSlot()) {
                    if (ANNIHILATOR_ARMOR.contains(id.toString())) {
                        UUID armorUUID = UUID.nameUUIDFromBytes(("linggango_leg_armor_" + event.getSlotType().getName()).getBytes());
                        UUID toughnessUUID = UUID.nameUUIDFromBytes(("linggango_leg_toughness_" + event.getSlotType().getName()).getBytes());
                        event.addModifier(Attributes.ARMOR, new AttributeModifier(armorUUID, "Legendary Armor Buff", 3.0, AttributeModifier.Operation.ADDITION));
                        event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(toughnessUUID, "Legendary Toughness Buff", 2.0, AttributeModifier.Operation.ADDITION));
                    }
                }
            }
        }
    }

    private static double getBonusDamage(String path) {


        return switch (path) {
            case "enderitium_sword", "enderitium_axe" -> 10.0;
            case "enderitium_pickaxe", "enderitium_shovel", "enderitium_hoe" -> 5.0;
            case "axe_of_lightning" -> 4.0;
            case "mossy_hammer" -> 3.0;
            case "the_tesseract" -> 25.0;
            default -> 2.0;
        };
    }
}