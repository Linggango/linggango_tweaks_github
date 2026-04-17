package com.misanthropy.linggango.linggango_tweaks.chaos;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class AntiCheeseHandler {

    private static final Map<UUID, PlayerPositionData> TRACKER = new HashMap<>();
    private static final int CHECK_INTERVAL = 20;
    private static final double STATIONARY_THRESHOLD = 1.5;
    private static final int WARNING_TIME = 30;
    private static final int PENALTY_TIME = 60;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide || !ChaosDifficultyAddon.isChaos(event.player.level())) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.player;
        if (player.isCreative() || player.isSpectator()) return;
        if (player.tickCount % CHECK_INTERVAL == 0) {
            UUID uuid = player.getUUID();
            Vec3 currentPos = player.position();
            PlayerPositionData data = TRACKER.computeIfAbsent(uuid, id -> new PlayerPositionData(currentPos));

            double distance = currentPos.distanceTo(data.lastPos);
            boolean hostilesNearby = !player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(24.0D),
                    entity -> entity instanceof Enemy && entity.isAlive()).isEmpty();

            if (distance < STATIONARY_THRESHOLD && hostilesNearby) {
                data.stationaryTime++;
            } else {
                data.stationaryTime = 0;
                data.lastPos = currentPos;
            }

            applyDesperationLogic(player, data);
        }
    }

    private static void applyDesperationLogic(ServerPlayer player, PlayerPositionData data) {
        if (data.stationaryTime >= PENALTY_TIME) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 1, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 100, 0, false, false));

            if (player.tickCount % 40 == 0) {
                player.displayClientMessage(Component.literal("§cYou are overcome by Desperation! Move!"), true);
            }
        } else if (data.stationaryTime >= WARNING_TIME) {
            if (data.stationaryTime % 5 == 0) {
                player.displayClientMessage(Component.literal("§6Standing still too long... Desperation approaches."), true);
            }
        }
    }

    private static class PlayerPositionData {
        Vec3 lastPos;
        int stationaryTime;

        PlayerPositionData(Vec3 pos) {
            this.lastPos = pos;
            this.stationaryTime = 0;
        }
    }
}