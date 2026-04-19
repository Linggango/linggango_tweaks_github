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
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BrutalityWeaponTweaks {

    private static final UUID BRUTALITY_BUFF_UUID = UUID.fromString("c3d4e5f6-a7b8-4c9d-0e1f-2a3b4c5d6e7f");

    @SubscribeEvent
    public static void onAttributeModifier(@NonNull ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND) return;

        ItemStack stack = event.getItemStack();
        ResourceLocation name = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (name != null && name.getNamespace().equals("brutality")) {
            double baseDamage = 0;
            Collection<AttributeModifier> modifiers = event.getModifiers().get(Attributes.ATTACK_DAMAGE);

            for (AttributeModifier mod : modifiers) {
                if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                    baseDamage += mod.getAmount();
                }
            }

            if (baseDamage <= 0) return;

            AttributeModifier modifier = getBrutalityModifier(name.getPath(), baseDamage);
            if (modifier != null) {
                event.addModifier(Attributes.ATTACK_DAMAGE, modifier);
            }
        }
    }

    private static @Nullable AttributeModifier getBrutalityModifier(@NonNull String path, double baseDamage) {
        double bonus = 0;

        if (path.equals("royal_guardian_sword")) {
            bonus = -150.0;
        } else if (path.contains("knife")) {
            if (baseDamage <= 9.0) {
                bonus = 4.0;
            } else if (baseDamage > 10.0) {
                bonus = 8.0;
            }
        } else if (path.equals("whisperwaltz")) {
            bonus = 8.0;
        } else {
            bonus = 20.0;
        }

        if (bonus == 0) return null;

        return new AttributeModifier(
                BRUTALITY_BUFF_UUID,
                "Brutality Bonus Damage",
                bonus,
                AttributeModifier.Operation.ADDITION
        );
    }
}