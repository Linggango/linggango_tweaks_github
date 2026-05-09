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
public class FuryBandTweak {

    private static final UUID FURY_BAND_RAGE_TIME_UUID = UUID.fromString("19f09575-a3df-425d-9ba1-3889a6c51290");
    private static final UUID FURY_BAND_RAGE_LEVEL_UUID = UUID.fromString("87c3e0a7-c9ea-4cd6-927d-a17509903caf");

    private static final UUID TIME_NERF_UUID = UUID.fromString("2af09575-a3df-425d-9ba1-3889a6c51291");
    private static final UUID LEVEL_NERF_UUID = UUID.fromString("98c3e0a7-c9ea-4cd6-927d-a17509903cb0");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;

        AttributeInstance rageTimeAttr = player.getAttribute(ModAttributes.RAGE_TIME_MULTIPLIER.get());
        if (rageTimeAttr != null) {
            boolean hasOriginal = rageTimeAttr.getModifier(FURY_BAND_RAGE_TIME_UUID) != null;
            AttributeModifier correction = rageTimeAttr.getModifier(TIME_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    rageTimeAttr.addTransientModifier(new AttributeModifier(
                            TIME_NERF_UUID,
                            "Fury Band Time Nerf",
                            -0.65D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            } else if (correction != null) {
                rageTimeAttr.removeModifier(TIME_NERF_UUID);
            }
        }

        AttributeInstance rageLevelAttr = player.getAttribute(ModAttributes.RAGE_LEVEL.get());
        if (rageLevelAttr != null) {
            boolean hasOriginal = rageLevelAttr.getModifier(FURY_BAND_RAGE_LEVEL_UUID) != null;
            AttributeModifier correction = rageLevelAttr.getModifier(LEVEL_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    rageLevelAttr.addTransientModifier(new AttributeModifier(
                            LEVEL_NERF_UUID,
                            "Fury Band Level Removal",
                            -1.0D,
                            AttributeModifier.Operation.ADDITION));
                }
            } else if (correction != null) {
                rageLevelAttr.removeModifier(LEVEL_NERF_UUID);
            }
        }
    }
}