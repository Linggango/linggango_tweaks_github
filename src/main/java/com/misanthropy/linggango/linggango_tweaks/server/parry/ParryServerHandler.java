package com.misanthropy.linggango.linggango_tweaks.server.parry;

import com.misanthropy.linggango.difficulty_enhancement.LinggangoEvents;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.network.parry.ParryNetwork;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParryServerHandler {

    public static final Map<UUID, PlayerParryData> playerDataMap = new ConcurrentHashMap<>();

    private static MobEffect   POSTURE_BREAK;
    private static MobEffect   VULNERABILITY;
    private static EntityType<?> APOSTLE_TYPE;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new ParryServerHandler());
    }

    public static void resetCombo(UUID playerId) {
        PlayerParryData data = playerDataMap.get(playerId);
        if (data != null) {
            data.comboStage = 0;
            data.comboLastParryTick = 0L;
            data.comboLastTargetId = null;
        }
    }

    public static class PlayerParryData {
        public boolean hasActiveParry = false;
        public long parryStartTick;
        public int parryTickMod;
        public int parryPerfectMod;
        public int parrySuccessMod;

        public int comboStage = 0;
        public long comboLastParryTick = 0L;
        public UUID comboLastTargetId = null;

        public boolean hasRecentHit = false;
        public long hitTick;
        public float hitAmount;
        public Entity hitAttacker;
        public Entity hitDirectEntity;

        public long lastParrySuccessTick = 0L;
        public long cooldownEndTick = 0L;
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        POSTURE_BREAK = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.fromNamespaceAndPath("soulsweapons", "posture_break"));
        VULNERABILITY = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.fromNamespaceAndPath("morerelics", "vulnerability"));
        APOSTLE_TYPE  = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.fromNamespaceAndPath("goety", "apostle"));
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        playerDataMap.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onPlayerDeath(@NonNull LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            resetCombo(player.getUUID());
            playerDataMap.remove(player.getUUID());
        }
    }

    public static void onClientParryPacket(ServerPlayer player) {
        long currentTick = player.level().getGameTime();
        UUID playerId  = player.getUUID();
        PlayerParryData data = playerDataMap.computeIfAbsent(playerId, k -> new PlayerParryData());

        long activeTicks = TweaksConfig.PARRY_STARTUP_TICKS.get() + TweaksConfig.PARRY_ACTIVE_WINDOW.get();
        if (data.hasActiveParry) {
            long duration = currentTick - data.parryStartTick;
            if (duration < activeTicks) {
                return;
            } else {
                data.hasActiveParry = false;
                data.cooldownEndTick = data.parryStartTick + activeTicks + TweaksConfig.PARRY_COOLDOWN.get();
            }
        }

        if (currentTick < data.cooldownEndTick) {
            return;
        }

        int tickMod = 0, perfectMod = 0, successMod = 0;
        LinggangoEvents.DifficultyDef diff = LinggangoEvents.getCurrentDifficulty(player.level());
        if (diff != null) {
            switch (diff.id) {
                case "cozy"    -> { tickMod = 2;  perfectMod = 1;  successMod = 1; }
                case "easy"    -> { tickMod = 1;  perfectMod = 1; }
                case "veteran" -> { tickMod = -1; successMod = -1; }
                case "extreme", "chaos", "torture" -> { tickMod = -2; perfectMod = -1; successMod = -1; }
                default -> {}
            }
        }

        data.hasActiveParry = true;
        data.parryStartTick = currentTick;
        data.parryTickMod = tickMod;
        data.parryPerfectMod = perfectMod;
        data.parrySuccessMod = successMod;

        ParryNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player),
                new ParryNetwork.S2CParryStartPacket(player.getId()));

        if (data.hasRecentHit && !player.isDeadOrDying() && player.getHealth() > 0.0f) {
            int latencyTicks = Math.min(player.latency / 50, 5) + 2;
            if (currentTick - data.hitTick <= latencyTicks) {
                data.hasActiveParry = false;
                data.hasRecentHit = false;
                Entity attacker = data.hitAttacker;
                Entity direct = data.hitDirectEntity;
                float amt = data.hitAmount;
                data.hitAttacker = null;
                data.hitDirectEntity = null;
                player.heal(amt);
                executeParrySuccess(player, data, attacker, direct, amt, 3, false);
            }
        }
    }

    @SubscribeEvent
    public void onLivingAttack(@NonNull LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DamageSource source = event.getSource();

        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD)
                || source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR)
                || source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            return;
        }

        if (player.isDeadOrDying() || player.getHealth() <= 0.0f) return;

        UUID playerId = player.getUUID();
        PlayerParryData data = playerDataMap.computeIfAbsent(playerId, k -> new PlayerParryData());
        long nowTicks = player.level().getGameTime();

        long activeTicks = TweaksConfig.PARRY_ACTIVE_WINDOW.get();
        boolean isWithinGrace = (data.lastParrySuccessTick > 0) && (nowTicks - data.lastParrySuccessTick < activeTicks);

        if (isWithinGrace) {
            event.setCanceled(true);
            executeParrySuccess(player, data, source.getEntity(), source.getDirectEntity(), event.getAmount(), 2, true);
            return;
        }

        if (source.getEntity() == null) return;
        if (APOSTLE_TYPE != null && source.getEntity().getType() == APOSTLE_TYPE) return;

        if (!data.hasActiveParry) {
            data.hasRecentHit = true;
            data.hitTick = nowTicks;
            data.hitAmount = event.getAmount();
            data.hitAttacker = source.getEntity();
            data.hitDirectEntity = source.getDirectEntity();
            return;
        }

        long elapsedTicks   = nowTicks - data.parryStartTick;
        int  startupTicks   = TweaksConfig.PARRY_STARTUP_TICKS.get();
        int  baseActiveTicks = TweaksConfig.PARRY_ACTIVE_WINDOW.get();
        long maxWindowTicks = startupTicks + baseActiveTicks + 20;

        if (elapsedTicks > maxWindowTicks) {
            data.hasActiveParry = false;
            data.hasRecentHit = true;
            data.hitTick = nowTicks;
            data.hitAmount = event.getAmount();
            data.hitAttacker = source.getEntity();
            data.hitDirectEntity = source.getDirectEntity();
            return;
        }

        long activeTicksLimit = Math.max(3, Math.min(9, baseActiveTicks + data.parryTickMod));

        if (elapsedTicks < startupTicks || elapsedTicks >= (startupTicks + activeTicksLimit)) {
            data.hasRecentHit = true;
            data.hitTick = nowTicks;
            data.hitAmount = event.getAmount();
            data.hitAttacker = source.getEntity();
            data.hitDirectEntity = source.getDirectEntity();
            return;
        }

        event.setCanceled(true);
        data.hasActiveParry = false;

        long activeElapsed = elapsedTicks - startupTicks;
        int  perfectTicks  = Math.max(1, TweaksConfig.PARRY_PERFECT_TICKS.get() + data.parryPerfectMod);
        int  successTicks  = Math.max(1, TweaksConfig.PARRY_SUCCESS_TICKS.get() + data.parrySuccessMod);

        int tier;
        if      (activeElapsed < perfectTicks)                tier = 3;
        else if (activeElapsed < perfectTicks + successTicks) tier = 2;
        else                                                  tier = 1;

        executeParrySuccess(player, data, source.getEntity(), source.getDirectEntity(), event.getAmount(), tier, false);
    }

    private static void executeParrySuccess(ServerPlayer player, PlayerParryData data, Entity attacker, Entity directEntity,
                                            float originalDamage, int tier, boolean isGraceDeflection) {
        long nowTicks       = player.level().getGameTime();
        UUID currentTargetId = attacker != null ? attacker.getUUID() : null;

        data.lastParrySuccessTick = nowTicks;
        data.cooldownEndTick = 0;

        boolean isSameTarget = data.comboLastTargetId == null || data.comboLastTargetId.equals(currentTargetId);

        if (nowTicks - data.comboLastParryTick <= 200L && isSameTarget) {
            data.comboStage = Math.min(8, data.comboStage + 1);
        } else {
            data.comboStage = 1;
        }
        data.comboLastParryTick = nowTicks;
        data.comboLastTargetId = currentTargetId;

        float heal = TweaksConfig.PARRY_HEAL_AMOUNT.get().floatValue();
        if (heal > 0.0f) player.heal(heal);

        if (directEntity instanceof Projectile projectile && TweaksConfig.PROJECTILE_DEFLECT_ENABLED.get()) {
            if (player.getRandom().nextFloat() < TweaksConfig.PROJECTILE_DEFLECT_CHANCE.get()) {
                Vec3 vel   = projectile.getDeltaMovement();
                double speed = vel.length();
                if (speed > 0.0) {
                    Entity owner  = projectile.getOwner();
                    Vec3 newDir   = (owner != null)
                            ? owner.getEyePosition().subtract(projectile.position()).normalize()
                            : player.getLookAngle();
                    projectile.setDeltaMovement(newDir.scale(speed * TweaksConfig.PROJECTILE_DEFLECT_SPEED.get()));
                    projectile.setOwner(player);
                    projectile.hasImpulse = true;
                }
            } else {
                projectile.discard();
            }
        }

        if (attacker instanceof LivingEntity livingAttacker && attacker != player) {
            double kb = tier == 3 ? 1.8 : 0.9;
            livingAttacker.knockback(kb,
                    player.getX() - livingAttacker.getX(),
                    player.getZ() - livingAttacker.getZ());

            if (!isGraceDeflection) {
                int currentStage = data.comboStage;
                float calculatedDamage;
                DamageSource damageSource;

                switch (currentStage) {
                    case 1  -> { calculatedDamage = 5.0f;  damageSource = player.damageSources().playerAttack(player); }
                    case 2  -> { calculatedDamage = 10.0f; damageSource = player.damageSources().playerAttack(player); }
                    case 3  -> { calculatedDamage = 15.0f; damageSource = player.damageSources().playerAttack(player); }
                    case 4  -> { calculatedDamage = 15.0f; damageSource = player.damageSources().indirectMagic(player, player); }
                    case 5  -> { calculatedDamage = 20.0f; damageSource = player.damageSources().indirectMagic(player, player); }
                    case 6  -> { calculatedDamage = 30.0f; damageSource = player.damageSources().indirectMagic(player, player); }
                    case 7  -> { calculatedDamage = 35.0f; damageSource = player.damageSources().indirectMagic(player, player); }
                    default -> { calculatedDamage = 40.0f; damageSource = player.damageSources().indirectMagic(player, player); }
                }

                if (tier == 3) calculatedDamage *= 1.3f;

                livingAttacker.hurt(damageSource, calculatedDamage);
                livingAttacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, true));

                if (VULNERABILITY != null)
                    livingAttacker.addEffect(new MobEffectInstance(VULNERABILITY, 60, 0, false, true));
                if (POSTURE_BREAK != null)
                    livingAttacker.addEffect(new MobEffectInstance(POSTURE_BREAK, 60, 0, false, true));
            }
        }

        if (tier != 3 && originalDamage > 150.0F) {
            Vec3 look = player.getLookAngle();
            player.knockback(1.5, look.x, look.z);

            AreaEffectCloud cloud = new AreaEffectCloud(player.level(),
                    player.getX(), player.getY(), player.getZ());
            cloud.setOwner(player);
            cloud.setRadius(1.5F);
            cloud.setRadiusOnUse(0.0F);
            cloud.setWaitTime(0);
            cloud.setDuration(200);
            cloud.setParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()));
            player.level().addFreshEntity(cloud);
        }

        ParryNetwork.CHANNEL.send(
                PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new ParryNetwork.S2CParrySuccessPacket(player.getId(), tier, data.comboStage));
    }
}