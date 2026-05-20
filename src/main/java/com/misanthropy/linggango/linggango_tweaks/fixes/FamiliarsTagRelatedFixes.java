package com.misanthropy.linggango.linggango_tweaks.fixes;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import net.alshanex.familiarslib.entity.AbstractSpellCastingPet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@SuppressWarnings("all")
public class FamiliarsTagRelatedFixes {

    public static final class AllianceResolver {
        public static boolean isFamiliarOrSummon(Entity entity) {
            return entity instanceof AbstractSpellCastingPet || entity instanceof IMagicSummon;
        }

        public static boolean areAllied(Entity left, Entity right) {
            if (left == null || right == null) return false;
            if (left == right) return true;

            UUID leftOwner = OwnerIdentityResolver.getCanonicalOwnerUuid(left);
            UUID rightOwner = OwnerIdentityResolver.getCanonicalOwnerUuid(right);

            if (leftOwner != null && leftOwner.equals(rightOwner)) return true;
            if (leftOwner != null && leftOwner.equals(right.getUUID())) return true;
            if (rightOwner != null && rightOwner.equals(left.getUUID())) return true;

            Entity leftOwnerEntity = OwnerIdentityResolver.getOwnerEntity(left);
            Entity rightOwnerEntity = OwnerIdentityResolver.getOwnerEntity(right);

            if (leftOwnerEntity != null && leftOwnerEntity.getUUID().equals(right.getUUID())) return true;
            if (rightOwnerEntity != null && rightOwnerEntity.getUUID().equals(left.getUUID())) return true;

            return leftOwnerEntity != null && rightOwnerEntity != null && leftOwnerEntity.getUUID().equals(rightOwnerEntity.getUUID());
        }

        public static boolean shouldBlockFriendlyInteraction(Entity source, Entity target) {
            return isFamiliarOrSummon(source) && isFamiliarOrSummon(target) && areAllied(source, target);
        }
    }

    public static final class OwnerIdentityResolver {
        public static UUID getCanonicalOwnerUuid(Entity entity) {
            if (entity == null) return null;
            if (entity instanceof Player player) return player.getUUID();

            UUID directOwner = getDirectOwnerUuid(entity);
            if (directOwner != null) return directOwner;

            Entity ownerEntity = getOwnerEntity(entity);
            return ownerEntity != null ? ownerEntity.getUUID() : null;
        }

        public static UUID getDirectOwnerUuid(Entity entity) {
            if (entity == null) return null;
            if (entity instanceof AbstractSpellCastingPet familiar) return familiar.getOwnerUUID();

            Entity ownerEntity = getOwnerEntity(entity);
            return ownerEntity != null ? ownerEntity.getUUID() : null;
        }

        public static Entity getOwnerEntity(Entity entity) {
            if (entity == null) return null;
            if (entity instanceof IMagicSummon summon) {
                Entity summoner = summon.getSummoner();
                if (summoner != null) return summoner;
            }
            if (entity instanceof AbstractSpellCastingPet familiar) {
                LivingEntity summoner = familiar.getSummoner();
                if (summoner != null) return summoner;
            }
            return null;
        }
    }

    public static final class TagReadinessService {
        private static final AtomicBoolean READY = new AtomicBoolean(false);
        private static final AtomicInteger EPOCH = new AtomicInteger(0);

        public static boolean isReady(Level level) {
            return level != null && !level.isClientSide && READY.get();
        }

        public static boolean areSpellTagsReady(Level level) {
            return level != null && !level.isClientSide && isSpellRegistryTagManagerReady();
        }

        public static int getEpoch() {
            return EPOCH.get();
        }

        @SubscribeEvent
        public static void onServerAboutToStart(ServerAboutToStartEvent event) { markNotReady(); }

        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) { markNotReady(); }

        @SubscribeEvent
        public static void onReloadBegin(AddReloadListenerEvent event) { markNotReady(); }

        @SubscribeEvent
        public static void onTagsUpdated(TagsUpdatedEvent event) {
            if (ServerLifecycleHooks.getCurrentServer() != null && event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
                READY.set(true);
                EPOCH.incrementAndGet();
            }
        }

        @SubscribeEvent
        public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
            if (ServerLifecycleHooks.getCurrentServer() != null) markNotReady();
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) { markNotReady(); }

        @SubscribeEvent
        public static void onServerStopped(ServerStoppedEvent event) { markNotReady(); }

        private static void markNotReady() {
            READY.set(false);
            EPOCH.incrementAndGet();
        }

        private static boolean isSpellRegistryTagManagerReady() {
            try {
                Supplier<?> supplier = SpellRegistry.REGISTRY;
                return supplier != null && supplier.get() instanceof IForgeRegistry<?> forgeRegistry && forgeRegistry.tags() != null;
            } catch (Throwable ignored) {
                return false;
            }
        }
    }

    public static final class FriendlyTargetAndDamageGuards {
        @SubscribeEvent
        public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
            if (AllianceResolver.shouldBlockFriendlyInteraction(event.getEntity(), event.getNewTarget())) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onLivingAttack(LivingAttackEvent event) {
            if (AllianceResolver.shouldBlockFriendlyInteraction(getDamageDealer(event.getSource()), event.getEntity())) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (AllianceResolver.shouldBlockFriendlyInteraction(getDamageDealer(event.getSource()), event.getEntity())) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onLivingDamage(LivingDamageEvent event) {
            if (AllianceResolver.shouldBlockFriendlyInteraction(getDamageDealer(event.getSource()), event.getEntity())) {
                event.setCanceled(true);
            }
        }

        private static Entity getDamageDealer(DamageSource source) {
            Entity attacker = source.getEntity();
            return attacker != null ? attacker : source.getDirectEntity();
        }
    }
}