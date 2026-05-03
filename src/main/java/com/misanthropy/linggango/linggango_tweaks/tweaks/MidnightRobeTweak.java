package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MidnightRobeTweak {

    @SubscribeEvent
    public static void onDealtDamage(LivingHurtEvent event) {

        if (event.getSource().getEntity() instanceof Player player) {
            if (player.level().isClientSide()) return;

            LivingEntity victim = event.getEntity();

            if (victim == player) return;

            CuriosApi.getCuriosHelper().findFirstCurio(player, stack ->
                    stack.getItem().getClass().getSimpleName().equals("MidnightRobeItem")
            ).ifPresent(slotResult -> {
                ItemStack robe = slotResult.stack();

                robe.getOrCreateTag().putString("target", victim.getUUID().toString());

            });
        }
    }
}