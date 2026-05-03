package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.network.NetworkHandler;
import com.misanthropy.linggango.linggango_tweaks.network.PerfectHitPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class PerfectHitCombatHandler {

    private static final Map<UUID, Long> COOLDOWN_MAP = new HashMap<>();
    private static final long COOLDOWN_TICKS = 600;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;

        if (event.getSource().getEntity() instanceof Player player) {

            boolean isFalling = !player.onGround() && player.fallDistance > 0.0F && !player.onClimbable() && !player.isInWater();

            if (isFalling) {
                long currentTime = player.level().getGameTime();
                long lastHitTime = COOLDOWN_MAP.getOrDefault(player.getUUID(), 0L);

                if (currentTime - lastHitTime >= COOLDOWN_TICKS) {
                    double chance = TweaksConfig.PERFECT_HIT_CHANCE.get();

                    if (player.getRandom().nextFloat() < chance) {

                        COOLDOWN_MAP.put(player.getUUID(), currentTime);

                        float originalDamage = event.getAmount();
                        float multiplier = TweaksConfig.PERFECT_HIT_DAMAGE_MULT.get().floatValue();
                        event.setAmount(originalDamage * multiplier);

                        LivingEntity target = event.getEntity();
                        float kbMult = TweaksConfig.PERFECT_HIT_KNOCKBACK_MULT.get().floatValue();
                        Vec3 lookDir = player.getLookAngle().normalize();

                        target.hasImpulse = true;
                        target.setDeltaMovement(target.getDeltaMovement().add(lookDir.x * kbMult, 0.5D, lookDir.z * kbMult));

                        if (player instanceof ServerPlayer serverPlayer) {
                            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new PerfectHitPacket());
                        }
                    }
                }
            }
        }
    }
}