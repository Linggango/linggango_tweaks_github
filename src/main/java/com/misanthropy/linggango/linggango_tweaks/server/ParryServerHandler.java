package com.misanthropy.linggango.linggango_tweaks.server;

import com.misanthropy.linggango.difficulty_enhancement.LinggangoEvents;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.parry.ParryNetwork;
import com.misanthropy.linggango.linggango_tweaks.registry.ModParticles;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class ParryServerHandler {

    public static final Map<UUID, Long> activeParries = new ConcurrentHashMap<>();
    public static final Map<UUID, ParryCombo> comboTracker = new ConcurrentHashMap<>();

    private static final long MS_PER_TICK = 50L;
    private static final long COMBO_TIMEOUT_MS = 10000L;

    private static MobEffect POSTURE_BREAK;
    private static MobEffect VULNERABILITY;
    private static EntityType<?> APOSTLE_TYPE;
    private static volatile boolean initialized = false;

    public static class ParryCombo {
        public int stage;
        public long lastParryMs;
        public ParryCombo(int stage, long lastParryMs) {
            this.stage = stage;
            this.lastParryMs = lastParryMs;
        }
    }

    private static void initCaches() {
        if (initialized) return;
        MobEffect FRAGILITY = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.fromNamespaceAndPath("mowziesmobs", "fragility"));
        POSTURE_BREAK = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.fromNamespaceAndPath("soulsweapons", "posture_break"));
        VULNERABILITY = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.fromNamespaceAndPath("morerelics", "vulnerability"));
        APOSTLE_TYPE  = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.fromNamespaceAndPath("goety", "apostle"));
        initialized = true;
    }

    public static void startParry(@NonNull UUID playerId) {
        activeParries.put(playerId, System.currentTimeMillis());
    }

    @SubscribeEvent
    public static void onLivingAttack(@NonNull LivingAttackEvent event) {
        initCaches();

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        DamageSource source = event.getSource();
        if (source.getEntity() == null) {
            return;
        }

        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD) ||
                source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR) ||
                source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            return;
        }

        Entity attackerCheck = source.getEntity();
        if (attackerCheck != null && attackerCheck.getType() == APOSTLE_TYPE) {
            return;
        }

        UUID playerId = player.getUUID();
        Long startMs = activeParries.get(playerId);
        if (startMs == null) {
            return;
        }

        long now = System.currentTimeMillis();
        long elapsed = now - startMs;

        int startupTicks  = TweaksConfig.PARRY_STARTUP_TICKS.get();
        int baseActiveTicks = TweaksConfig.PARRY_ACTIVE_WINDOW.get();
        long startupMs    = startupTicks * MS_PER_TICK;
        long baseActiveMs = baseActiveTicks * MS_PER_TICK;

        long maxWindowMs = startupMs + baseActiveMs + (20 * MS_PER_TICK);
        if (elapsed > maxWindowMs) {
            activeParries.remove(playerId);
            return;
        }

        int latencyCompMs = Math.min(player.latency, 250);

        int tickMod = 0, perfectMod = 0, successMod = 0;
        LinggangoEvents.DifficultyDef diff = LinggangoEvents.getCurrentDifficulty(player.level());
        if (diff != null) {
            switch (diff.id) {
                case "cozy"     -> { tickMod = 2;  perfectMod = 1;  successMod = 1; }
                case "easy"     -> { tickMod = 1;  perfectMod = 1; }
                case "veteran"  -> { tickMod = -1; successMod = -1; }
                case "extreme"  -> { tickMod = -2; perfectMod = -1; successMod = -1; }
                case "torture"  -> { tickMod = -3; perfectMod = -1; successMod = -2; }
                case "chaos"    -> { tickMod = -4; perfectMod = -2; successMod = -2; }
                default -> { }
            }
        }

        long activeMs = Math.max(3 * MS_PER_TICK,
                Math.min(9 * MS_PER_TICK, baseActiveMs + (tickMod * MS_PER_TICK)));
        long compensatedActiveMs = activeMs + latencyCompMs;

        if (elapsed < startupMs || elapsed >= (startupMs + compensatedActiveMs)) {
            return;
        }

        event.setCanceled(true);
        activeParries.remove(playerId);

        long activeElapsed = elapsed - startupMs;
        long compensatedElapsed = Math.max(0, activeElapsed - (latencyCompMs / 2));

        int perfectTicks  = Math.max(1, TweaksConfig.PARRY_PERFECT_TICKS.get() + perfectMod);
        int successTicks  = Math.max(1, TweaksConfig.PARRY_SUCCESS_TICKS.get() + successMod);
        long perfectMs    = perfectTicks * MS_PER_TICK;
        long successMs    = successTicks * MS_PER_TICK;

        int tier;
        if (compensatedElapsed < perfectMs) {
            tier = 3;
        } else if (compensatedElapsed < perfectMs + successMs) {
            tier = 2;
        } else {
            tier = 1;
        }

        ParryCombo combo = comboTracker.computeIfAbsent(playerId, k -> new ParryCombo(0, 0));
        if (now - combo.lastParryMs <= COMBO_TIMEOUT_MS) {
            combo.stage = Math.min(8, combo.stage + 1);
        } else {
            combo.stage = 1;
        }
        combo.lastParryMs = now;

        Entity directEntity = source.getDirectEntity();
        Entity attacker = source.getEntity();

        float heal = TweaksConfig.PARRY_HEAL_AMOUNT.get().floatValue();
        if (heal > 0.0f) {
            player.heal(heal);
        }

        if (directEntity instanceof Projectile projectile && TweaksConfig.PROJECTILE_DEFLECT_ENABLED.get()) {
            if (player.getRandom().nextFloat() < TweaksConfig.PROJECTILE_DEFLECT_CHANCE.get()) {
                Entity owner = projectile.getOwner();
                Vec3 vel = projectile.getDeltaMovement();
                double speed = vel.length();
                Vec3 newDir = (owner != null)
                        ? owner.getEyePosition().subtract(projectile.position()).normalize()
                        : player.getLookAngle();

                projectile.setDeltaMovement(newDir.scale(speed * TweaksConfig.PROJECTILE_DEFLECT_SPEED.get()));
                projectile.setOwner(player);
                projectile.hasImpulse = true;
            } else {
                projectile.discard();
            }
        }

        if (attacker instanceof LivingEntity livingAttacker && attacker != player) {
            double kb = tier == 3 ? 1.8 : 0.9;
            livingAttacker.knockback(kb,
                    player.getX() - livingAttacker.getX(),
                    player.getZ() - livingAttacker.getZ());

            livingAttacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, true));

            if (VULNERABILITY != null) {
                livingAttacker.addEffect(new MobEffectInstance(VULNERABILITY, 60, 0, false, true));
            }
            if (POSTURE_BREAK != null) {
                livingAttacker.addEffect(new MobEffectInstance(POSTURE_BREAK, 60, 0, false, true));
            }

            float calculatedDamage;
            if (combo.stage >= 8) {
                calculatedDamage = livingAttacker.getMaxHealth() * 0.03f;
            } else {
                float[] flatDamages = {2f, 4f, 6f, 6f, 6f, 6f, 7f};
                calculatedDamage = flatDamages[combo.stage - 1];
            }

            if (tier == 3) calculatedDamage *= 1.3f;

            livingAttacker.hurt(player.damageSources().playerAttack(player), calculatedDamage);
        }

        if (tier != 3 && event.getAmount() > 150.0F) {
            Vec3 look = player.getLookAngle();
            player.knockback(1.5, look.x, look.z);

            AreaEffectCloud cloud = new AreaEffectCloud(player.level(), player.getX(), player.getY(), player.getZ());
            cloud.setOwner(player);
            cloud.setRadius(1.5F);
            cloud.setRadiusOnUse(0.0F);
            cloud.setWaitTime(0);
            cloud.setDuration(200);
            cloud.setParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()));
            player.level().addFreshEntity(cloud);
        }

        spawnParrySparkles(player, tier, combo.stage);

        ParryNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new ParryNetwork.S2CParrySuccessPacket(player.getId(), tier, combo.stage));
    }

    private static void spawnParrySparkles(@NonNull Player player, int tier, int comboStage) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        double px = player.getX();
        double py = player.getY() + player.getEyeHeight() - 0.2;
        double pz = player.getZ();
        Vec3 look = player.getLookAngle();

        double ix = px + look.x * 1.8;
        double iy = py + look.y * 1.8;
        double iz = pz + look.z * 1.8;

        float comboMultiplier = 1.0f + ((comboStage - 1) / 7.0f);

        RandomSource rand = level.random;
        int lines = (int) ((1 + rand.nextInt(2)) * comboMultiplier);
        double baseSpeed = (0.02 + rand.nextDouble() * 0.02) * comboMultiplier;

        for (int l = 0; l < lines; l++) {
            double angle = rand.nextDouble() * Math.PI * 2.0;
            double dx = Math.cos(angle);
            double dz = Math.sin(angle);

            double up = Math.toRadians(45 + rand.nextDouble() * 15);
            double hSpeed = baseSpeed * Math.cos(up);
            double vSpeed = baseSpeed * Math.sin(up);

            level.sendParticles(
                    ModParticles.PARRY_SPARKLE.get(),
                    ix, iy, iz, 0,
                    dx * hSpeed, vSpeed, dz * hSpeed,
                    1.0
            );
        }

        SimpleParticleType particle = tier == 3
                ? ModParticles.PERFECT_PARRY.get()
                : ModParticles.ANIMATED_PARRY.get();

        level.sendParticles(particle, ix, iy, iz, 1, 0, 0, 0, 0);
    }
}