package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.goo.brutality.registry.ModAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodPulseGauntletsTweak {

    private static final UUID BLOOD_PULSE_RAGE_LEVEL_UUID = UUID.fromString("64f0a078-b7a3-4956-b0cf-fd02fdb9d0a6");

    private static final UUID RAGE_LEVEL_NERF_UUID = UUID.fromString("75f0a078-b7a3-4956-b0cf-fd02fdb9d0a7");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;
        AttributeInstance rageLevelAttr = player.getAttribute(ModAttributes.RAGE_LEVEL.get());

        if (rageLevelAttr != null) {
            boolean hasOriginal = rageLevelAttr.getModifier(BLOOD_PULSE_RAGE_LEVEL_UUID) != null;
            AttributeModifier correction = rageLevelAttr.getModifier(RAGE_LEVEL_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    rageLevelAttr.addTransientModifier(new AttributeModifier(
                            RAGE_LEVEL_NERF_UUID,
                            "Blood Pulse Gauntlets Balanced Level",
                            -1.0D,
                            AttributeModifier.Operation.ADDITION));
                }
            } else if (correction != null) {
                rageLevelAttr.removeModifier(RAGE_LEVEL_NERF_UUID);
            }
        }
    }
}