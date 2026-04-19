package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID)
public class SpawnMiddleware {

    private static final Map<ResourceLocation, Integer> GLOBAL_SPAWN_COUNTS = new ConcurrentHashMap<>();
    private static int totalTrackedSpawns = 0;

    @SubscribeEvent
    public static void onCheckSpawn(MobSpawnEvent.@NonNull FinalizeSpawn event) {
        LevelAccessor level = event.getLevel();
        Mob entity = event.getEntity();
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());

        if (id == null) return;

        if (level instanceof ServerLevelAccessor serverLevel && serverLevel.getLevel().dimension() == Level.OVERWORLD) {

            if (entity.getType().getCategory() != MobCategory.MONSTER && ThreadLocalRandom.current().nextFloat() < 0.005f) {
                EntityType<?> hostileType = getStarvingMobType(MobCategory.MONSTER);
                if (replaceSpawn(serverLevel, entity, hostileType, event)) return;
            }

            if (ThreadLocalRandom.current().nextFloat() < 0.005f) {
                EntityType<?> absoluteWildcard = getAbsoluteStarvingMobType();
                if (replaceSpawn(serverLevel, entity, absoluteWildcard, event)) return;
            }

            AABB box = new AABB(entity.blockPosition()).inflate(64.0);
            List<Mob> nearbySameType = level.getEntitiesOfClass(Mob.class, box, e -> e.getType() == entity.getType());

            if (nearbySameType.size() > 4) {
                EntityType<?> alternativeType = getStarvingMobType(entity.getType().getCategory());
                if (!replaceSpawn(serverLevel, entity, alternativeType, event)) {
                    event.setSpawnCancelled(true);
                    event.setCanceled(true);
                }
                return;
            }

            String namespace = id.getNamespace();
            if (!namespace.equals("minecraft")) {
                List<Mob> nearbySameMod = level.getEntitiesOfClass(Mob.class, box, e -> {
                    ResourceLocation eId = ForgeRegistries.ENTITY_TYPES.getKey(e.getType());
                    return eId != null && eId.getNamespace().equals(namespace);
                });

                if (nearbySameMod.size() > 15) {
                    event.setSpawnCancelled(true);
                    event.setCanceled(true);
                    return;
                }
            }
        }

        int currentGlobalCount = GLOBAL_SPAWN_COUNTS.getOrDefault(id, 0);

        if (currentGlobalCount > 15) {
            float failChance = Math.min(0.85f, (currentGlobalCount - 15) * 0.05f);
            if (ThreadLocalRandom.current().nextFloat() < failChance) {
                event.setSpawnCancelled(true);
                event.setCanceled(true);
                return;
            }
        }

        GLOBAL_SPAWN_COUNTS.put(id, currentGlobalCount + 1);
        totalTrackedSpawns++;

        if (totalTrackedSpawns >= 200) {
            decaySpawnCounts();
        }
    }

    private static boolean replaceSpawn(@NonNull ServerLevelAccessor level, @NonNull Mob original, @Nullable EntityType<?> newType, MobSpawnEvent.@NonNull FinalizeSpawn event) {
        if (newType != null && newType != original.getType()) {
            Entity wildcard = newType.create(level.getLevel());
            if (wildcard instanceof Mob wildcardMob) {
                wildcardMob.moveTo(original.getX(), original.getY(), original.getZ(), original.getYRot(), original.getXRot());
                level.addFreshEntity(wildcardMob);
                event.setSpawnCancelled(true);
                event.setCanceled(true);
                return true;
            }
        }
        return false;
    }

    private static @Nullable EntityType<?> getStarvingMobType(MobCategory targetCategory) {
        EntityType<?> starvingType = null;
        int lowestCount = Integer.MAX_VALUE;

        for (EntityType<?> type : SpawnChanges.TWEAKED_ENTITIES) {
            if (type.getCategory() == targetCategory) {
                ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(type);
                if (typeId != null) {
                    int count = GLOBAL_SPAWN_COUNTS.getOrDefault(typeId, 0);
                    if (count < lowestCount) {
                        lowestCount = count;
                        starvingType = type;
                    } else if (count == lowestCount && ThreadLocalRandom.current().nextBoolean()) {
                        starvingType = type;
                    }
                }
            }
        }
        return starvingType;
    }

    private static @Nullable EntityType<?> getAbsoluteStarvingMobType() {
        EntityType<?> starvingType = null;
        int lowestCount = Integer.MAX_VALUE;

        for (EntityType<?> type : SpawnChanges.TWEAKED_ENTITIES) {
            ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (typeId != null) {
                int count = GLOBAL_SPAWN_COUNTS.getOrDefault(typeId, 0);
                if (count < lowestCount) {
                    lowestCount = count;
                    starvingType = type;
                } else if (count == lowestCount && ThreadLocalRandom.current().nextBoolean()) {
                    starvingType = type;
                }
            }
        }
        return starvingType;
    }

    private static void decaySpawnCounts() {
        for (Map.Entry<ResourceLocation, Integer> entry : GLOBAL_SPAWN_COUNTS.entrySet()) {
            int decayedValue = entry.getValue() / 2;
            if (decayedValue <= 0) {
                GLOBAL_SPAWN_COUNTS.remove(entry.getKey());
            } else {
                GLOBAL_SPAWN_COUNTS.put(entry.getKey(), decayedValue);
            }
        }
        totalTrackedSpawns = 0;
    }
}