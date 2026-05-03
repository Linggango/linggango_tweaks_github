package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.goo.brutality.registry.BrutalityModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RampageClockTweak {

    private static final int MAX_RAGE_DURATION = 200;

    @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            capRampageDuration(player);
        }
    }

    private static void capRampageDuration(Player player) {
        CuriosApi.getCuriosHelper().findFirstCurio(player, stack ->
                stack.getItem().getClass().getSimpleName().equals("RampageClock")
        ).ifPresent(slotResult -> {
            MobEffectInstance enraged = player.getEffect(BrutalityModMobEffects.ENRAGED.get());

            if (enraged != null && enraged.getDuration() > MAX_RAGE_DURATION) {

                player.addEffect(new MobEffectInstance(
                        BrutalityModMobEffects.ENRAGED.get(),
                        MAX_RAGE_DURATION,
                        enraged.getAmplifier(),
                        enraged.isAmbient(),
                        enraged.isVisible(),
                        enraged.showIcon()
                ));
            }
        });
    }
}