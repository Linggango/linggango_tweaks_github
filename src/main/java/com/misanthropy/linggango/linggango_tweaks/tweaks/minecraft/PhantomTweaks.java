package com.misanthropy.linggango.linggango_tweaks.tweaks.minecraft;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PhantomTweaks {

    private static final int TICKS_PER_DAY = 24000;
    private static final int TWO_DAYS_TICKS = TICKS_PER_DAY * 2;

    @SubscribeEvent
    public static void onPhantomSpawn(MobSpawnEvent.FinalizeSpawn event) {

        if (!(event.getEntity() instanceof Phantom phantom) || event.getLevel().isClientSide()) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getLevel().getNearestPlayer(phantom, 128.0D);

        if (player != null) {
            int timeSinceRest = player.getStats().getValue(Stats.CUSTOM, Stats.TIME_SINCE_REST);

            if (!shouldSpawn(timeSinceRest)) {
                event.setSpawnCancelled(true);
                event.setResult(Event.Result.DENY);
            }
        }
    }

    private static boolean shouldSpawn(int ticks) {

        if (ticks < TWO_DAYS_TICKS) {
            return false;
        }

        int cycleProgress = ticks % (TWO_DAYS_TICKS * 2);
        return cycleProgress >= TWO_DAYS_TICKS && cycleProgress < (TWO_DAYS_TICKS + TICKS_PER_DAY);
    }
}