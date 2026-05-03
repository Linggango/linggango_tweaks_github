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
public class HeartOfWrathTweak {

    private static final UUID WRATH_HEART_MAX_RAGE_UUID = UUID.fromString("30edfb9a-d882-45eb-8962-d8734abfb01c");
    private static final UUID WRATH_HEART_RAGE_GAIN_UUID = UUID.fromString("5201e697-66e7-4698-977f-49da5625199b");

    private static final UUID MAX_RAGE_NERF_UUID = UUID.fromString("41edfb9a-d882-45eb-8962-d8734abfb01d");
    private static final UUID RAGE_GAIN_NERF_UUID = UUID.fromString("6301e697-66e7-4698-977f-49da5625199c");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;

        AttributeInstance maxRageAttr = player.getAttribute(ModAttributes.MAX_RAGE.get());
        if (maxRageAttr != null) {
            boolean hasOriginal = maxRageAttr.getModifier(WRATH_HEART_MAX_RAGE_UUID) != null;
            AttributeModifier correction = maxRageAttr.getModifier(MAX_RAGE_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    maxRageAttr.addTransientModifier(new AttributeModifier(
                            MAX_RAGE_NERF_UUID,
                            "Heart of Wrath Max Rage Nerf",
                            -120.0D,
                            AttributeModifier.Operation.ADDITION));
                }
            } else if (correction != null) {
                maxRageAttr.removeModifier(MAX_RAGE_NERF_UUID);
            }
        }

        AttributeInstance rageGainAttr = player.getAttribute(ModAttributes.RAGE_GAIN_MULTIPLIER.get());
        if (rageGainAttr != null) {
            boolean hasOriginal = rageGainAttr.getModifier(WRATH_HEART_RAGE_GAIN_UUID) != null;
            AttributeModifier correction = rageGainAttr.getModifier(RAGE_GAIN_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    rageGainAttr.addTransientModifier(new AttributeModifier(
                            RAGE_GAIN_NERF_UUID,
                            "Heart of Wrath Rage Gain Nerf",
                            -0.85D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            } else if (correction != null) {
                rageGainAttr.removeModifier(RAGE_GAIN_NERF_UUID);
            }
        }
    }
}