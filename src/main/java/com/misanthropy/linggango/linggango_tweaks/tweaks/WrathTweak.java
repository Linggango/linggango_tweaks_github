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
public class WrathTweak {

    private static final UUID WRATH_RAGE_UUID = UUID.fromString("2d029ff6-aae3-424f-950e-5cd68a96d6c0");
    private static final UUID WRATH_RAGE_TIME_UUID = UUID.fromString("e6f8b7ec-d379-403d-8cb6-7e51a15dca09");
    private static final UUID WRATH_RAGE_GAIN_UUID = UUID.fromString("7d91e73a-6d7a-4576-8dfe-845898a6c63b");

    private static final UUID MAX_RAGE_NERF_UUID = UUID.fromString("3d029ff6-aae3-424f-950e-5cd68a96d6c1");
    private static final UUID RAGE_TIME_NERF_UUID = UUID.fromString("f7f8b7ec-d379-403d-8cb6-7e51a15dca10");
    private static final UUID RAGE_GAIN_NERF_UUID = UUID.fromString("8e91e73a-6d7a-4576-8dfe-845898a6c63c");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;

        AttributeInstance maxRageAttr = player.getAttribute(ModAttributes.MAX_RAGE.get());
        if (maxRageAttr != null) {
            boolean hasOriginal = maxRageAttr.getModifier(WRATH_RAGE_UUID) != null;
            AttributeModifier correction = maxRageAttr.getModifier(MAX_RAGE_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    maxRageAttr.addTransientModifier(new AttributeModifier(
                            MAX_RAGE_NERF_UUID,
                            "Wrath Max Rage Nerf",
                            -35.0D,
                            AttributeModifier.Operation.ADDITION));
                }
            } else if (correction != null) {
                maxRageAttr.removeModifier(MAX_RAGE_NERF_UUID);
            }
        }

        AttributeInstance rageTimeAttr = player.getAttribute(ModAttributes.RAGE_TIME_MULTIPLIER.get());
        if (rageTimeAttr != null) {
            boolean hasOriginal = rageTimeAttr.getModifier(WRATH_RAGE_TIME_UUID) != null;
            AttributeModifier correction = rageTimeAttr.getModifier(RAGE_TIME_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    rageTimeAttr.addTransientModifier(new AttributeModifier(
                            RAGE_TIME_NERF_UUID,
                            "Wrath Rage Time Nerf",
                            -0.15D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            } else if (correction != null) {
                rageTimeAttr.removeModifier(RAGE_TIME_NERF_UUID);
            }
        }

        AttributeInstance rageGainAttr = player.getAttribute(ModAttributes.RAGE_GAIN_MULTIPLIER.get());
        if (rageGainAttr != null) {
            boolean hasOriginal = rageGainAttr.getModifier(WRATH_RAGE_GAIN_UUID) != null;
            AttributeModifier correction = rageGainAttr.getModifier(RAGE_GAIN_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    rageGainAttr.addTransientModifier(new AttributeModifier(
                            RAGE_GAIN_NERF_UUID,
                            "Wrath Rage Gain Nerf",
                            -0.15D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            } else if (correction != null) {
                rageGainAttr.removeModifier(RAGE_GAIN_NERF_UUID);
            }
        }
    }
}