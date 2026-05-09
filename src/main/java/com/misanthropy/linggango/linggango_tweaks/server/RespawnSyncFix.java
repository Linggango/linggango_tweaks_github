package com.misanthropy.linggango.linggango_tweaks.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class RespawnSyncFix {

    private static final Map<UUID, Integer> queue = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.@NonNull PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            queue.put(player.getUUID(), 40);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.@NonNull ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || queue.isEmpty()) return;

        var iterator = queue.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            int timeLeft = entry.getValue() - 1;

            if (timeLeft <= 0) {
                iterator.remove();
                ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    player.teleportTo(player.serverLevel(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                }
            } else {
                queue.put(entry.getKey(), timeLeft);
            }
        }
    }
}