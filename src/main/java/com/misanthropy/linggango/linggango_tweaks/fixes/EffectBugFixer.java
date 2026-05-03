package com.misanthropy.linggango.linggango_tweaks.fixes;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jspecify.annotations.NonNull;


@Mod.EventBusSubscriber(modid  = LinggangoTweaks.MOD_ID)
public class EffectBugFixer {

    private static final int MAX_SAFE_DURATION = 19200;
    private static int tickTimer = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.@NonNull ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickTimer++;

        if (tickTimer >= 1200) {
            tickTimer = 0;

            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                java.util.List<net.minecraft.world.effect.MobEffect> effectsToRemove = new java.util.ArrayList<>();

                for (MobEffectInstance instance : player.getActiveEffects()) {
                    int duration = instance.getDuration();
                    if (duration > MAX_SAFE_DURATION || duration == -1) {
                        effectsToRemove.add(instance.getEffect());
                    }
                }

                for (net.minecraft.world.effect.MobEffect effect : effectsToRemove) {
                    player.removeEffect(effect);
                }
            }
        }
    }
}