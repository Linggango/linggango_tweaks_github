package com.misanthropy.linggango.linggango_tweaks.events;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;


@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE) public class DamageFix {
    @SubscribeEvent
    public static void onLivingHurt(@NonNull LivingHurtEvent e) {
        if (Float.isNaN(e.getAmount())) {
            e.setCanceled(true);
            rectify(e.getEntity());
        }
    }
    @SubscribeEvent
    public static void onLivingDamage(@NonNull LivingDamageEvent e) {
        if (Float.isNaN(e.getAmount())) {
            e.setCanceled(true);
            rectify(e.getEntity());
        }
    }
    @SubscribeEvent
    public static void onAttackEntity(@NonNull LivingAttackEvent e) {
        if (Float.isNaN(e.getAmount())) { e.setCanceled(true);
            rectify(e.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingHeal(@NonNull LivingHealEvent e) {
        LivingEntity le = e.getEntity();
        if (Float.isNaN(e.getAmount())) {
            e.setCanceled(true);
        } else if (Float.isNaN(le.getHealth())) {
            e.setCanceled(true); rectify(le);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(@NonNull LivingDeathEvent e) {
        if (Float.isNaN(e.getEntity().getHealth())) {
            e.setCanceled(true);
            rectify(e.getEntity());
        }
    } private static void rectify(@NonNull LivingEntity le) {
        le.setHealth(le.getMaxHealth());
        le.setAbsorptionAmount(0.0F);
    }
}