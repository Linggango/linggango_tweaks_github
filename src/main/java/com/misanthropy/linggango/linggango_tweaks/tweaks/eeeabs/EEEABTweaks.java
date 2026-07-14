package com.misanthropy.linggango.linggango_tweaks.tweaks.eeeabs;

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

    private static final UUID DAMAGE_MODIFIER_UUID = UUID.nameUUIDFromBytes("linggango_eeeab_damage_mainhand".getBytes());
    private static final AttributeModifier DAMAGE_MODIFIER = new AttributeModifier(DAMAGE_MODIFIER_UUID, "EEEAB Weapon Damage Buff", 6.0, AttributeModifier.Operation.ADDITION);

    @SubscribeEvent
    public static void onAttributes(ItemAttributeModifierEvent event) {
        if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            Item item = event.getItemStack().getItem();
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id != null && "eeeabsmobs".equals(id.getNamespace())) {
                if (item instanceof TieredItem || item instanceof TridentItem) {
                    event.addModifier(Attributes.ATTACK_DAMAGE, DAMAGE_MODIFIER);
                }
            }
        }
    }
}