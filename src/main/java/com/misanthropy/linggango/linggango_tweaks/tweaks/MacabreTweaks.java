package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MacabreTweaks {
    private static final Map<String, Double> WEAPON_BUFFS = Map.ofEntries(
            Map.entry("macabre:hemorrhage_sword", 20.0),
            Map.entry("macabre:true_gorescythe", 40.0),
            Map.entry("macabre:chanisword", 21.0),
            Map.entry("macabre:the_cleave", 18.0),
            Map.entry("macabre:valamon_sickle", 25.0),
            Map.entry("macabre:valamon_axe", 45.0),
            Map.entry("macabre:valamon_gut_whip", 25.0),
            Map.entry("macabre:spearplunder", 10.0),
            Map.entry("macabre:shorgan", 3.0),

            // dont ask why ts here
            Map.entry("monsterexpansion:horn_of_the_shattercry", 6.0),
            Map.entry("monsterexpansion:duskrend", 10.0),
            Map.entry("monsterexpansion:rimescourge", 10.0),
            Map.entry("monsterexpansion:spire_shell_bulwark", 5.0)
    );

    @SubscribeEvent
    public static void onAttributes(ItemAttributeModifierEvent event) {
        if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            Item item = event.getItemStack().getItem();
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null) {
                Double bonusDamage = WEAPON_BUFFS.get(id.toString());
                if (bonusDamage != null) {
                    UUID damageUUID = UUID.nameUUIDFromBytes(("linggango_macabre_damage_" + event.getSlotType().getName()).getBytes());
                    event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(damageUUID, "Macabre Damage Buff", bonusDamage, AttributeModifier.Operation.ADDITION));
                }
            }
        }
    }
}