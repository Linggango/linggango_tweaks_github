package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HeartOfGoldTweak {
    private static final float MAX_ABSORPTION = 6.0F;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player victim) {
            capHeartOfGold(victim);
        }
    }

    private static void capHeartOfGold(Player player) {
        CuriosApi.getCuriosHelper().findFirstCurio(player, stack ->
                stack.getItem().getClass().getSimpleName().equals("HeartOfGold")
        ).ifPresent(slotResult -> {
            if (player.getAbsorptionAmount() > MAX_ABSORPTION) {
                player.setAbsorptionAmount(MAX_ABSORPTION);
            }
        });
    }
}