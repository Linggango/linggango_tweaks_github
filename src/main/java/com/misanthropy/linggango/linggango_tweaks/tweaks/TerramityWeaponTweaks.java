package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TerramityWeaponTweaks {

    private static final UUID TERRAMITY_BUFF_UUID = UUID.fromString("a1b2c3d4-e5f6-4a5b-bc6d-7e8f9a0b1c2d");

    @SubscribeEvent
    public static void onAttributeModifier(@NonNull ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND) return;

        ItemStack stack = event.getItemStack();
        ResourceLocation name = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (name != null && name.getNamespace().equals("terramity")) {
            double baseDamage = 0;
            Collection<AttributeModifier> modifiers = event.getModifiers().get(Attributes.ATTACK_DAMAGE);

            for (AttributeModifier mod : modifiers) {
                if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                    baseDamage += mod.getAmount();
                }
            }

            if (baseDamage <= 0) return;

            AttributeModifier modifier = getAttributeModifier(baseDamage);
            event.addModifier(Attributes.ATTACK_DAMAGE, modifier);
        }
    }

    private static @NonNull AttributeModifier getAttributeModifier(double baseDamage) {
        double bonus = 3.0;

        if (baseDamage >= 30.0) {
            bonus = 10.0;
        } else if (baseDamage >= 20.0) {
            bonus = 5.0;
        } else if (baseDamage >= 8.0 && baseDamage <= 15.0) {
            bonus = 4.0;
        }

        return new AttributeModifier(
                TERRAMITY_BUFF_UUID,
                "Terramity Bonus Damage",
                bonus,
                AttributeModifier.Operation.ADDITION
        );
    }
}