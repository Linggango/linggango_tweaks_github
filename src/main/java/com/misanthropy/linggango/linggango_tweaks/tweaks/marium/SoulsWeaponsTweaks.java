package com.misanthropy.linggango.linggango_tweaks.tweaks.marium;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoulsWeaponsTweaks {

    private static final UUID WEAPON_BUFF_UUID = UUID.fromString("91b15d2a-4a27-4a6c-9a4f-561b36d0e6fb");
    private static final UUID ARMOR_BUFF_UUID = UUID.fromString("d288b8d9-1c95-46eb-8e50-9856cc46b288");
    private static final UUID TOUGHNESS_BUFF_UUID = UUID.fromString("b40e24ec-1e24-4f4c-8822-19e0cf558450");

    private static final Map<String, Double> WEAPON_DAMAGE_BUFFS = Map.ofEntries(
            Map.entry("bluemoon_shortsword", 5.0),
            Map.entry("bluemoon_greatsword", 7.0),
            Map.entry("moonlight_shortsword", 8.0),
            Map.entry("moonlight_greatsword", 12.0),
            Map.entry("pure_moonlight_greatsword", 18.0),
            Map.entry("bloodthirster", 8.0),
            Map.entry("darkin_blade", 13.0),
            Map.entry("dragon_staff", 6.0),
            Map.entry("withered_wabbajack", 5.0),
            Map.entry("dragonslayer_swordspear", 14.0),
            Map.entry("rageblade", 11.0),
            Map.entry("nightfall", 12.0),
            Map.entry("lich_bane", 12.0),
            Map.entry("dawnbreaker", 5.0),
            Map.entry("soul_reaper", 11.0),
            Map.entry("forlorn_scythe", 11.0),
            Map.entry("leviathan_axe", 6.0),
            Map.entry("skofnung", 6.0),
            Map.entry("mjolnir", 11.0),
            Map.entry("freyr_sword", 5.0),
            Map.entry("crucible_sword", 28.0),
            Map.entry("darkin_scythe_pre", 7.0),
            Map.entry("darkin_scythe", 11.0),
            Map.entry("shadow_assassin_scythe", 11.0),
            Map.entry("holy_greatsword", 8.0),
            Map.entry("draupnir_spear", 8.0),
            Map.entry("holy_moonlight_greatsword", 19.0),
            Map.entry("holy_moonlight_sword", 12.0),
            Map.entry("frostmourne", 10.0),
            Map.entry("master_sword", 8.0),
            Map.entry("nights_edge_item", 8.0),
            Map.entry("empowered_dawnbreaker", 11.0),
            Map.entry("chungus_staff", 6.0),
            Map.entry("dark_moon_greatsword", 20.0),
            Map.entry("glaive_of_hodir", 6.0),
            Map.entry("excalibur", 9.0),
            Map.entry("supernova", 8.0)
    );

    @SubscribeEvent
    public static void onAttributeModification(ItemAttributeModifierEvent event) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        if (id == null || !id.getNamespace().equals("soulsweapons")) return;

        String path = id.getPath();

        if (path.equals("chaos_robes") && event.getSlotType() == EquipmentSlot.CHEST) {
            event.addModifier(Attributes.ARMOR, new AttributeModifier(
                    ARMOR_BUFF_UUID, "Chaos Robes Armor Buff", 1.0, AttributeModifier.Operation.ADDITION));
            event.addModifier(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(
                    TOUGHNESS_BUFF_UUID, "Chaos Robes Toughness Buff", 1.0, AttributeModifier.Operation.ADDITION));
            return;

        }

        if (event.getSlotType() == EquipmentSlot.MAINHAND) {

            if (event.getOriginalModifiers().containsKey(Attributes.ATTACK_DAMAGE)) {

                double damageBuff = WEAPON_DAMAGE_BUFFS.getOrDefault(path, 3.0);

                event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                        WEAPON_BUFF_UUID, "SoulsWeapons Tweaks Buff", damageBuff, AttributeModifier.Operation.ADDITION));
            }
        }
    }
}