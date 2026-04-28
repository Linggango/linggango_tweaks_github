package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.goo.brutality.registry.ModAttributes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BattleScarsTweak {

    private static final UUID BATTLE_SCARS_RAGE_GAIN_UUID = UUID.fromString("551269f9-4bd5-4a74-a03d-55be778f39bc");

    private static final UUID BATTLE_SCARS_NERF_UUID = UUID.fromString("661269f9-4bd5-4a74-a03d-55be778f39bd");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;
        AttributeInstance rageGainAttr = player.getAttribute(ModAttributes.RAGE_GAIN_MULTIPLIER.get());

        if (rageGainAttr != null) {
            AttributeModifier originalMod = rageGainAttr.getModifier(BATTLE_SCARS_RAGE_GAIN_UUID);

            if (originalMod != null) {

                List<MobEffectInstance> harmfulEffects = player.getActiveEffects().stream()
                        .filter(effect -> !effect.getEffect().isBeneficial())
                        .toList();

                int count = harmfulEffects.size();
                if (count > 0) {

                    double correctionValue = count * -0.12D;

                    AttributeModifier existingNerf = rageGainAttr.getModifier(BATTLE_SCARS_NERF_UUID);
                    if (existingNerf == null || existingNerf.getAmount() != correctionValue) {
                        rageGainAttr.removeModifier(BATTLE_SCARS_NERF_UUID);
                        rageGainAttr.addTransientModifier(new AttributeModifier(
                                BATTLE_SCARS_NERF_UUID,
                                "Battle Scars Balanced Gain",
                                correctionValue,
                                AttributeModifier.Operation.MULTIPLY_TOTAL));
                    }
                } else {
                    rageGainAttr.removeModifier(BATTLE_SCARS_NERF_UUID);
                }
            } else {

                if (rageGainAttr.getModifier(BATTLE_SCARS_NERF_UUID) != null) {
                    rageGainAttr.removeModifier(BATTLE_SCARS_NERF_UUID);
                }
            }
        }
    }
}