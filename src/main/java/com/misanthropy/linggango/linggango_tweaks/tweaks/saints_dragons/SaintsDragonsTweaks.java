package com.misanthropy.linggango.linggango_tweaks.tweaks.saints_dragons;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.Random;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SaintsDragonsTweaks {

    private static final Random RANDOM = new Random();
    private static final String MOD_ID = "saintsdragons";

    @SubscribeEvent
    public static void onDragonSpawn(MobSpawnEvent.FinalizeSpawn event) {
        Entity entity = event.getEntity();
        if (entity == null) return;

        EntityType<?> type = entity.getType();
        ResourceLocation registryName = ForgeRegistries.ENTITY_TYPES.getKey(type);

        if (registryName == null || !registryName.getNamespace().equals(MOD_ID)) {
            return;
        }

        if (event.getLevel().isClientSide()) {
            return;
        }

        if (RANDOM.nextInt(3) != 0) {
            event.setSpawnCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }
}