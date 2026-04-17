package com.misanthropy.linggango.linggango_tweaks.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class RespawnSyncFix {

    private static final Map<UUID, Integer> playersToSync = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            playersToSync.put(player.getUUID(), 40);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (playersToSync.isEmpty()) return;

        net.minecraft.server.MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (Map.Entry<UUID, Integer> entry : playersToSync.entrySet()) {
            UUID uuid = entry.getKey();
            int ticksLeft = entry.getValue() - 1;

            if (ticksLeft <= 0) {
                playersToSync.remove(uuid);

                ServerPlayer player = server.getPlayerList().getPlayer(uuid);
                if (player != null) {
                    net.minecraft.server.level.ServerLevel level = player.serverLevel();
                    player.teleportTo(level, player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                }
            } else {
                playersToSync.put(uuid, ticksLeft);
            }
        }
    }
}