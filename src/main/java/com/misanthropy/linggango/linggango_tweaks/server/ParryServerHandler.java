package com.misanthropy.linggango.linggango_tweaks.server;

import com.misanthropy.linggango.difficulty_enhancement.LinggangoEvents;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.misanthropy.linggango.linggango_tweaks.network.ParryNetwork;
import com.misanthropy.linggango.linggango_tweaks.registry.ModParticles;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
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

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class ParryServerHandler {
    public static final Map<UUID, Long> activeParries = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onLivingAttack(@NonNull LivingAttackEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            if (event.getSource().getEntity() == null) {
                return;
            }

            if (event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_SHIELD) ||
                    event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR) ||
                    event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
                return;
            }

            Entity attackerCheck = event.getSource().getEntity();
            ResourceLocation attackerId = ForgeRegistries.ENTITY_TYPES.getKey(attackerCheck.getType());
            if (attackerId != null && attackerId.toString().equals("goety:apostle")) {
                return;
            }

            Long startTime = activeParries.get(player.getUUID());
            if (startTime != null) {
                long elapsed = player.level().getGameTime() - startTime;
                int startup = TweaksConfig.PARRY_STARTUP_TICKS.get();
                int baseActive = TweaksConfig.PARRY_ACTIVE_WINDOW.get();

                int tickModifier = 0;
                int perfectMod = 0;
                int successMod = 0;

                LinggangoEvents.DifficultyDef diff = LinggangoEvents.getCurrentDifficulty(player.level());
                if (diff != null) {
                    switch (diff.id) {
                        case "cozy": tickModifier = 2; perfectMod = 1; successMod = 1; break;
                        case "easy": tickModifier = 1; perfectMod = 1;
                            break;
                        case "normal":
                            break;
                        case "veteran": tickModifier = -1;
                            successMod = -1; break;
                        case "extreme": tickModifier = -2; perfectMod = -1; successMod = -1; break;
                        case "torture": tickModifier = -3; perfectMod = -1; successMod = -2; break;
                        case "chaos": tickModifier = -4; perfectMod = -2; successMod = -2; break;
                    }
                }

                int active = Math.max(3, Math.min(9, baseActive + tickModifier));

                if (elapsed >= startup && elapsed < (startup + active)) {
                    event.setCanceled(true);

                    long activeElapsed = elapsed - startup;
                    int perfectTicks = Math.max(1, TweaksConfig.PARRY_PERFECT_TICKS.get() + perfectMod);
                    int successTicks = Math.max(1, TweaksConfig.PARRY_SUCCESS_TICKS.get() + successMod);

                    int tier = 1;
                    if (activeElapsed < perfectTicks) {
                        tier = 3;
                    } else if (activeElapsed < perfectTicks + successTicks) {
                        tier = 2;
                    }

                    Entity directEntity = event.getSource().getDirectEntity();
                    Entity attacker = event.getSource().getEntity();

                    float healAmt = TweaksConfig.PARRY_HEAL_AMOUNT.get().floatValue();
                    if (healAmt > 0) {
                        player.heal(healAmt);
                    }

                    if (directEntity instanceof Projectile projectile && TweaksConfig.PROJECTILE_DEFLECT_ENABLED.get()) {
                        if (player.getRandom().nextFloat() < TweaksConfig.PROJECTILE_DEFLECT_CHANCE.get()) {
                            Entity owner = projectile.getOwner();
                            Vec3 currentVelocity = projectile.getDeltaMovement();
                            double speed = currentVelocity.length();
                            Vec3 newDirection;

                            if (owner != null) {
                                newDirection = owner.getEyePosition().subtract(projectile.position()).normalize();
                            } else {
                                newDirection = player.getLookAngle();
                            }

                            projectile.setDeltaMovement(newDirection.scale(speed * TweaksConfig.PROJECTILE_DEFLECT_SPEED.get()));
                            projectile.setOwner(player);
                            projectile.hasImpulse = true;
                        } else {
                            projectile.discard();
                        }
                    }

                    if (attacker instanceof LivingEntity livingAttacker && attacker != player) {
                        double kbStrength = tier == 3 ? 1.8 : 0.9;
                        livingAttacker.knockback(kbStrength, player.getX() - livingAttacker.getX(), player.getZ() - livingAttacker.getZ());

                        int slownessDuration = tier == 3 ? 80 : 30;
                        int slownessAmp = tier == 3 ? 1 : 0;
                        livingAttacker.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, slownessDuration, slownessAmp, false, true));

                        int effectDuration = tier == 3 ? 100 : 40;

                        MobEffect fragility = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.fromNamespaceAndPath("mowziesmobs", "fragility"));
                        if (fragility != null) {
                            livingAttacker.addEffect(new MobEffectInstance(fragility, effectDuration, 0, false, true));
                        }

                        MobEffect postureBreak = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.fromNamespaceAndPath("soulsweapons", "posture_break"));
                        if (postureBreak != null) {
                            livingAttacker.addEffect(new MobEffectInstance(postureBreak, effectDuration, 0, false, true));
                        }

                        if (tier == 3) {
                            MobEffect vulnerability = ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.fromNamespaceAndPath("morerelics", "vulnerability"));
                            if (vulnerability != null) {
                                livingAttacker.addEffect(new MobEffectInstance(vulnerability, 100, 0, false, true));
                            }
                        }

                        float maxHp = livingAttacker.getMaxHealth();
                        float damagePercent = tier == 3 ? 0.04f : 0.02f;
                        livingAttacker.hurt(player.damageSources().playerAttack(player), maxHp * damagePercent);
                    }

                    if (tier != 3 && event.getAmount() > 150.0F) {
                        Vec3 look = player.getLookAngle();
                        player.knockback(1.5, look.x, look.z);

                        AreaEffectCloud dustCloud = new AreaEffectCloud(player.level(), player.getX(), player.getY(), player.getZ());
                        dustCloud.setOwner(player);
                        dustCloud.setRadius(1.5F);
                        dustCloud.setRadiusOnUse(0.0F);
                        dustCloud.setWaitTime(0);
                        dustCloud.setDuration(200);
                        dustCloud.setParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()));
                        player.level().addFreshEntity(dustCloud);
                    }

                    spawnParrySparkles(player, tier);

                    ParryNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new ParryNetwork.S2CParrySuccessPacket(player.getId(), tier));
                }
            }
        }
    }

    private static void spawnParrySparkles(@NonNull Player player, int tier) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 playerPos = player.position().add(0, player.getEyeHeight() - 0.2, 0);
            Vec3 lookVec = player.getLookAngle();
            Vec3 impactPos = playerPos.add(lookVec.scale(1.8));

            RandomSource random = serverLevel.random;
            int numLines = 1 + random.nextInt(2);
            double baseSpeed = 0.02 + random.nextDouble() * 0.02;

            for (int l = 0; l < numLines; l++) {
                double angle = random.nextDouble() * Math.PI * 2.0;

                double dx = Math.cos(angle);
                double dz = Math.sin(angle);

                double upwardAngle = Math.toRadians(45 + random.nextDouble() * 15);
                double horizontalSpeed = baseSpeed * Math.cos(upwardAngle);
                double verticalSpeed = baseSpeed * Math.sin(upwardAngle);

                Vec3 velocity = new Vec3(
                        dx * horizontalSpeed,
                        verticalSpeed,
                        dz * horizontalSpeed
                );

                serverLevel.sendParticles(ModParticles.PARRY_SPARKLE.get(), impactPos.x, impactPos.y, impactPos.z, 0, velocity.x, velocity.y, velocity.z, 1.0);
            }

            SimpleParticleType particleType = tier == 3 ? ModParticles.PERFECT_PARRY.get() : ModParticles.ANIMATED_PARRY.get();
            serverLevel.sendParticles(particleType, impactPos.x, impactPos.y, impactPos.z, 1, 0, 0, 0, 0);
        }
    }
}