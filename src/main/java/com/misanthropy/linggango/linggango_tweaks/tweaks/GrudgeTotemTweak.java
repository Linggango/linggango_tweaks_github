package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.goo.brutality.registry.BrutalityModMobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GrudgeTotemTweak {

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void undoImmortality(LivingDeathEvent event) {
        if (event.isCanceled() && event.getEntity() instanceof Player player) {

            if (player.getHealth() == 0.5F && player.hasEffect(BrutalityModMobEffects.ENRAGED.get())) {
                CuriosApi.getCuriosHelper().findFirstCurio(player, stack ->
                        stack.getItem().getClass().getSimpleName().equals("GrudgeTotem")
                ).ifPresent(slot -> {

                    event.setCanceled(false);
                    player.setHealth(0.0F);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) return;

        Player player = event.player;

        if (player.tickCount % 20 == 0 && player.hasEffect(BrutalityModMobEffects.ENRAGED.get())) {
            CuriosApi.getCuriosHelper().findFirstCurio(player, stack ->
                    stack.getItem().getClass().getSimpleName().equals("GrudgeTotem")
            ).ifPresent(slot -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, true)));
        }
    }
}