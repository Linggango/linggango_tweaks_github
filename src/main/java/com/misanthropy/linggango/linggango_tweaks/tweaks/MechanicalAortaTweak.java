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
public class MechanicalAortaTweak {

    private static final UUID MECH_AORTA_RAGE_GAIN_UUID = UUID.fromString("5a5cbb13-3506-4102-99c6-9c0b39bbb8c7");
    private static final UUID MECH_AORTA_RAGE_TIME_UUID = UUID.fromString("daa0998d-794d-4998-8734-0a77b161c4e4");

    private static final UUID GAIN_NERF_UUID = UUID.fromString("6b6dcc24-4617-5213-aa07-0d1c40ccc9d8");
    private static final UUID TIME_NERF_UUID = UUID.fromString("ebb1aa9e-8a5e-5009-9845-1b88c272d5f5");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;

        AttributeInstance rageGainAttr = player.getAttribute(ModAttributes.RAGE_GAIN_MULTIPLIER.get());
        if (rageGainAttr != null) {
            boolean hasOriginal = rageGainAttr.getModifier(MECH_AORTA_RAGE_GAIN_UUID) != null;
            AttributeModifier correction = rageGainAttr.getModifier(GAIN_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    rageGainAttr.addTransientModifier(new AttributeModifier(
                            GAIN_NERF_UUID,
                            "Mechanical Aorta Gain Nerf",
                            -1.8D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            } else if (correction != null) {
                rageGainAttr.removeModifier(GAIN_NERF_UUID);
            }
        }

        AttributeInstance rageTimeAttr = player.getAttribute(ModAttributes.RAGE_TIME_MULTIPLIER.get());
        if (rageTimeAttr != null) {
            boolean hasOriginal = rageTimeAttr.getModifier(MECH_AORTA_RAGE_TIME_UUID) != null;
            AttributeModifier correction = rageTimeAttr.getModifier(TIME_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    rageTimeAttr.addTransientModifier(new AttributeModifier(
                            TIME_NERF_UUID,
                            "Mechanical Aorta Time Nerf",
                            -0.1D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            } else if (correction != null) {
                rageTimeAttr.removeModifier(TIME_NERF_UUID);
            }
        }
    }
}