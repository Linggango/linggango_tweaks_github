package com.misanthropy.linggango.linggango_tweaks.tweaks.terramity;

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

    private static final AttributeModifier MODIFIER_3 = new AttributeModifier(TERRAMITY_BUFF_UUID, "Terramity Bonus Damage", 3.0, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier MODIFIER_4 = new AttributeModifier(TERRAMITY_BUFF_UUID, "Terramity Bonus Damage", 4.0, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier MODIFIER_5 = new AttributeModifier(TERRAMITY_BUFF_UUID, "Terramity Bonus Damage", 5.0, AttributeModifier.Operation.ADDITION);
    private static final AttributeModifier MODIFIER_10 = new AttributeModifier(TERRAMITY_BUFF_UUID, "Terramity Bonus Damage", 10.0, AttributeModifier.Operation.ADDITION);

    @SubscribeEvent
    public static void onAttributeModifier(@NonNull ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND) return;

        ItemStack stack = event.getItemStack();
        ResourceLocation name = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (name != null && "terramity".equals(name.getNamespace())) {
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
        if (baseDamage >= 30.0) {
            return MODIFIER_10;
        } else if (baseDamage >= 20.0) {
            return MODIFIER_5;
        } else if (baseDamage >= 8.0 && baseDamage <= 15.0) {
            return MODIFIER_4;
        }
        return MODIFIER_3;
    }
}