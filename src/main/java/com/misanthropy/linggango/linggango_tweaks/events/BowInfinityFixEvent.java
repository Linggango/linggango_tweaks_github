package com.misanthropy.linggango.linggango_tweaks.events;

import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.player.ArrowNockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BowInfinityFixEvent {

    @SubscribeEvent
    public static void linggango$infinityFix(ArrowNockEvent event) {
        if (event.getBow().getEnchantmentLevel(Enchantments.INFINITY_ARROWS) > 0) {
            event.getEntity().startUsingItem(event.getHand());
            event.setAction(InteractionResultHolder.consume(event.getBow()));
        }
    }
}