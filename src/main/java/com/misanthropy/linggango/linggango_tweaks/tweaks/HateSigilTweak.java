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
public class HateSigilTweak {

    private static final UUID HATE_SIGIL_MAX_RAGE_UUID = UUID.fromString("192e8875-c1fd-44ac-852b-c69903c53dd1");
    private static final UUID HATE_SIGIL_RAGE_GAIN_UUID = UUID.fromString("bee635ef-d345-4d24-9295-ff7fad619785");

    private static final UUID MAX_RAGE_NERF_UUID = UUID.fromString("2a2e8875-c1fd-44ac-852b-c69903c53dd2");
    private static final UUID RAGE_GAIN_NERF_UUID = UUID.fromString("cee635ef-d345-4d24-9295-ff7fad619786");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;

        AttributeInstance maxRageAttr = player.getAttribute(ModAttributes.MAX_RAGE.get());
        if (maxRageAttr != null) {
            boolean hasOriginal = maxRageAttr.getModifier(HATE_SIGIL_MAX_RAGE_UUID) != null;
            AttributeModifier correction = maxRageAttr.getModifier(MAX_RAGE_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    maxRageAttr.addTransientModifier(new AttributeModifier(
                            MAX_RAGE_NERF_UUID,
                            "Hate Sigil Max Rage Nerf",
                            -95.0D,
                            AttributeModifier.Operation.ADDITION));
                }
            } else if (correction != null) {
                maxRageAttr.removeModifier(MAX_RAGE_NERF_UUID);
            }
        }

        AttributeInstance rageGainAttr = player.getAttribute(ModAttributes.RAGE_GAIN_MULTIPLIER.get());
        if (rageGainAttr != null) {
            boolean hasOriginal = rageGainAttr.getModifier(HATE_SIGIL_RAGE_GAIN_UUID) != null;
            AttributeModifier correction = rageGainAttr.getModifier(RAGE_GAIN_NERF_UUID);

            if (hasOriginal) {
                if (correction == null) {

                    rageGainAttr.addTransientModifier(new AttributeModifier(
                            RAGE_GAIN_NERF_UUID,
                            "Hate Sigil Rage Gain Nerf",
                            -0.45D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));
                }
            } else if (correction != null) {
                rageGainAttr.removeModifier(RAGE_GAIN_NERF_UUID);
            }
        }
    }
}