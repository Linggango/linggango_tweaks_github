package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BafsWeaponTweaks {

    private static final UUID BAFS_DAMAGE_UUID = UUID.fromString("7f3e1b2a-5d4c-4b3a-9e8d-1f2a3b4c5d6e");
    private static final ResourceLocation PULVERIZED_ID = new ResourceLocation("brutality", "pulverized");

    @SubscribeEvent
    public static void onAttributeModifier(@NonNull ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlot.MAINHAND) return;

        Item item = event.getItemStack().getItem();
        ResourceLocation name = ForgeRegistries.ITEMS.getKey(item);
        if (isBafsWeapon(name) || isPicklePaddle(name)) {
            AttributeModifier modifier = new AttributeModifier(
                    BAFS_DAMAGE_UUID,
                    "Lethality Bonus Damage",
                    5.0,
                    AttributeModifier.Operation.ADDITION
            );
            event.addModifier(Attributes.ATTACK_DAMAGE, modifier);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(@NonNull LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
            ItemStack weapon = attacker.getMainHandItem();
            ResourceLocation name = ForgeRegistries.ITEMS.getKey(weapon.getItem());

            if (isBafsWeapon(name)) {
                LivingEntity victim = event.getEntity();

                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0));

                MobEffect pulverized = ForgeRegistries.MOB_EFFECTS.getValue(PULVERIZED_ID);
                if (pulverized != null) {
                    victim.addEffect(new MobEffectInstance(pulverized, 40, 2));
                }
            }
        }
    }

    private static boolean isBafsWeapon(@Nullable ResourceLocation name) {
        if (name == null || !name.getNamespace().equals("lethality")) return false;
        String path = name.getPath();
        return path.contains("_bafs") || path.contains("_bafpb");
    }

    private static boolean isPicklePaddle(@Nullable ResourceLocation name) {
        return name != null && name.getNamespace().equals("lethality") && name.getPath().equals("pickle_paddle");
    }
}