package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BloodStoneTweak {

    private static final int MAX_LIFESTEAL_DURATION = 10;

    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) return;

        MobEffectInstance instance = event.getEffectInstance();

        String effectName = instance.getEffect().getDescriptionId().toLowerCase();

        if (effectName.contains("lifesteal")) {
            CuriosApi.getCuriosHelper().findFirstCurio(player, stack ->
                    stack.getItem().getClass().getSimpleName().equals("BloodStone")
            ).ifPresent(slotResult -> {

                if (instance.getDuration() > MAX_LIFESTEAL_DURATION) {

                    player.addEffect(new MobEffectInstance(
                            instance.getEffect(),
                            MAX_LIFESTEAL_DURATION,
                            instance.getAmplifier(),
                            instance.isAmbient(),
                            instance.isVisible(),
                            instance.showIcon()
                    ));
                }
            });
        }
    }
}