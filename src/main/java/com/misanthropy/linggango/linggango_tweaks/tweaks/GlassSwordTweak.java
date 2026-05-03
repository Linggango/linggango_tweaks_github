package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GlassSwordTweak {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof LivingEntity attacker) {
            ItemStack stack = attacker.getMainHandItem();
            if (!stack.isEmpty() && stack.getItem().getClass().getSimpleName().equals("GlassSwordItem")) {
                attacker.level().playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(),
                        SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                attacker.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                stack.setCount(0);
            }
        }
    }
}