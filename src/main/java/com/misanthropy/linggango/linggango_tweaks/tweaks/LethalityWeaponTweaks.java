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
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LethalityWeaponTweaks {

    private static final UUID LETHALITY_BUFF_UUID = UUID.fromString("b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e");

    private static final Set<String> EXCLUDED_WEAPONS = Set.of(
            "lethality:hf_meowrasama",
            "lethality:starlight"
    );

    @SubscribeEvent
    public static void onAttributeModifier(@NonNull ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND) return;

        ItemStack stack = event.getItemStack();
        ResourceLocation name = ForgeRegistries.ITEMS.getKey(stack.getItem());

        if (name != null && name.getNamespace().equals("lethality")) {
            if (EXCLUDED_WEAPONS.contains(name.toString()) || isBafsWeapon(name)) return;

            double baseDamage = 0;
            Collection<AttributeModifier> modifiers = event.getModifiers().get(Attributes.ATTACK_DAMAGE);

            for (AttributeModifier mod : modifiers) {
                if (mod.getOperation() == AttributeModifier.Operation.ADDITION) {
                    baseDamage += mod.getAmount();
                }
            }

            if (baseDamage <= 0) return;

            AttributeModifier modifier = getLethalityModifier(name.getPath());
            event.addModifier(Attributes.ATTACK_DAMAGE, modifier);
        }
    }

    private static boolean isBafsWeapon(@NonNull ResourceLocation name) {
        String path = name.getPath();
        return path.contains("_bafs") || path.contains("_bafpb");
    }

    private static @NotNull AttributeModifier getLethalityModifier(@NonNull String path) {
        double bonus = 10.0;

        switch (path) {
            case "nightmare_sword" -> bonus += 50.0;
            case "violence" -> bonus += 20.0;
            case "horseless_headless_horsemanns_headtaker" -> bonus += 15.0;
            case "miasma", "sacrifice" -> bonus += 10.0;
            case "blighted_cleaver", "defiled_greatsword", "devils_devastation", "exalted_oathblade" -> bonus += 7.0;
            case "grievance", "gamblers_blade" -> bonus += 5.0;
        }

        return new AttributeModifier(
                LETHALITY_BUFF_UUID,
                "Lethality Bonus Damage",
                bonus,
                AttributeModifier.Operation.ADDITION
        );
    }
}