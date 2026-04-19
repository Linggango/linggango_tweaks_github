package com.misanthropy.linggango.linggango_tweaks.chaos;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NyxarisGlobalUnlocker {

    @SubscribeEvent
    public static void onBossDeath(@NonNull LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id != null && id.getPath().contains("nyxaris")) {
            GlobalProgressionHandler.unlockChaos();
        }
    }
}