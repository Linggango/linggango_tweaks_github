package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EEEABTweaks {

    @SubscribeEvent
    public static void onAttributes(ItemAttributeModifierEvent event) {
        if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            Item item = event.getItemStack().getItem();
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null && id.getNamespace().equals("eeeabsmobs")) {
                if (item instanceof TieredItem || item instanceof TridentItem) {
                    UUID damageUUID = UUID.nameUUIDFromBytes(("linggango_eeeab_damage_" + event.getSlotType().getName()).getBytes());
                    event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(damageUUID, "EEEAB Weapon Damage Buff", 6.0, AttributeModifier.Operation.ADDITION));
                }
            }
        }
    }
}