package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.goo.brutality.registry.BrutalityCapabilities;
import net.goo.brutality.registry.BrutalityModMobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AngerManagementTweak {
    private static final float ACTIVATION_THRESHOLD = 350.0F;

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getEffectInstance().getEffect() == BrutalityModMobEffects.ENRAGED.get()) {

            CuriosApi.getCuriosHelper().findFirstCurio(player, stack ->
                    stack.getItem().getClass().getSimpleName().equals("AngerManagement")
            ).ifPresent(slot -> player.getCapability(BrutalityCapabilities.PLAYER_RAGE_CAP).ifPresent(cap -> {

                if (cap.rageValue() < ACTIVATION_THRESHOLD) {
                    event.setResult(Event.Result.DENY);
                }
            }));
        }
    }
}