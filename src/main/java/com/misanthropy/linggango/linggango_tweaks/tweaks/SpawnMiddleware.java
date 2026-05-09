package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID)
public class SpawnMiddleware {

    private static final long CHUNK_TTL_MS = 600_000L;
    private static final Long2ObjectMap<ChunkSpawnData> CHUNK_DATA = new Long2ObjectOpenHashMap<>();
    private static final Set<String> BOSS_BLACKLIST = ConcurrentHashMap.newKeySet();

    private static final Map<MobCategory, List<WeightedType>> REPLACEMENT_POOLS = new HashMap<>();
    private static boolean poolsInitialized = false;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (++tickCounter % 1200 == 0) cleanupOldChunks();
    }

    @SubscribeEvent
    public static void onCheckSpawn(MobSpawnEvent.@NonNull FinalizeSpawn event) {
        if (!TweaksConfig.ENABLE_DYNAMIC_BALANCING.get()) return;
        if (event.getSpawnType() != MobSpawnType.NATURAL) return;

        Mob entity = event.getEntity();
        if (entity.getTags().contains("linggango_processed")) return;
        if (entity.level().isClientSide()) return;

        ServerLevelAccessor sla = event.getLevel();
        ServerLevel level = sla.getLevel();
        if (level.dimension() != Level.OVERWORLD) return;

        initPools();
        initBossBlacklist();

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id == null) return;
        if (BOSS_BLACKLIST.contains(id.toString())) {
            event.setSpawnCancelled(true);
            return;
        }

        boolean isVanilla = id.getNamespace().equals("minecraft");
        ChunkPos cPos = new ChunkPos(entity.blockPosition());
        ChunkSpawnData chunk = getChunkData(cPos);
        Holder<Biome> biome = level.getBiome(entity.blockPosition());

        entity.addTag("linggango_processed");
        SpawnStatistics.record(entity.getType());

        int sameType = chunk.getCount(entity.getType());
        int hardCap = TweaksConfig.HARD_CAP_PER_TYPE.get();
        int softCap = TweaksConfig.MAX_SAME_TYPE_PER_CHUNK.get();

        if (sameType >= hardCap) {
            EntityType<?> fallback = selectReplacement(
                    entity.getType().getCategory(), chunk, biome,
                    entity.blockPosition(), sla, false);

            if (fallback != null && fallback != entity.getType()
                    && trySpawnReplacement(entity, fallback, sla, event)) {
                chunk.record(fallback, isVanilla(fallback));
                SpawnStatistics.record(fallback);
                return;
            }
            event.setSpawnCancelled(true);
            return;
        }

        if (sameType >= softCap && !isVanilla) {
            EntityType<?> rot = selectReplacement(
                    entity.getType().getCategory(), chunk, biome,
                    entity.blockPosition(), sla, false);

            if (rot != null && rot != entity.getType()
                    && trySpawnReplacement(entity, rot, sla, event)) {
                chunk.record(rot, isVanilla(rot));
                SpawnStatistics.record(rot);
                return;
            }
        }

        if (isVanilla) {
            float moddedRatio = chunk.getModdedRatio();
            float targetRatio = (float) (double) TweaksConfig.TARGET_MODDED_RATIO.get();
            if (moddedRatio < targetRatio) {
                float deficit = 1.0f - (moddedRatio / targetRatio);
                float chance = 0.45f * deficit;
                if (ThreadLocalRandom.current().nextFloat() < chance) {
                    EntityType<?> inject = selectReplacement(
                            entity.getType().getCategory(), chunk, biome,
                            entity.blockPosition(), sla, true);

                    if (inject != null && inject != entity.getType() && !isVanilla(inject)
                            && trySpawnReplacement(entity, inject, sla, event)) {
                        chunk.record(inject, false);
                        SpawnStatistics.record(inject);
                        return;
                    }
                }
            }
        }

        chunk.record(entity.getType(), isVanilla);
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        CHUNK_DATA.clear();
        REPLACEMENT_POOLS.clear();
        poolsInitialized = false;
        BOSS_BLACKLIST.clear();
    }

    private static @Nullable EntityType<?> selectReplacement(
            MobCategory category,
            ChunkSpawnData chunk,
            Holder<Biome> biome,
            BlockPos pos,
            ServerLevelAccessor level,
            boolean preferModded
    ) {
        if (category == null) return null;

        List<WeightedType> pool = REPLACEMENT_POOLS.get(category);
        if (pool == null || pool.isEmpty()) return null;

        float[] weights = new float[pool.size()];
        float total = 0.0f;
        RandomSource random = level.getRandom();

        float unseenBonus = 3.0f;
        float seenLight = 0.6f;
        float seenHeavy = 0.15f;
        float globalBoost = 2.5f;
        float globalPenalty = 0.35f;
        float nativeMult = (float) (double) TweaksConfig.BIOME_NATIVE_MULTIPLIER.get();
        float foreignMult = (float) (double) TweaksConfig.BIOME_FOREIGN_MULTIPLIER.get();

        for (int i = 0; i < pool.size(); i++) {
            WeightedType wt = pool.get(i);
            float w = wt.baseWeight;

            int count = chunk.getCount(wt.type);
            if (count == 0)        w *= unseenBonus;
            else if (count <= 2)   w *= seenLight;
            else if (count <= 4)   w *= seenHeavy;
            else                   w *= 0.05f;

            double relFreq = SpawnStatistics.getRelativeFrequency(wt.type);
            if (relFreq < 0.5)       w *= globalBoost;
            else if (relFreq > 2.0)  w *= globalPenalty;
            else                     w *= (float) (1.0 / relFreq);

            if (BiomeAffinityCache.isNative(wt.type, biome)) {
                w *= nativeMult;
            } else {
                w *= foreignMult;
            }

            if (preferModded && wt.isVanilla) {
                w *= 0.08f;
            } else if (!preferModded && !wt.isVanilla) {
                w *= 0.5f;
            }

            ResourceLocation rid = ForgeRegistries.ENTITY_TYPES.getKey(wt.type);
            if (rid != null && BOSS_BLACKLIST.contains(rid.toString())) w = 0f;

            weights[i] = w;
            total += w;
        }

        if (total <= 0.0f) return null;

        float roll = ThreadLocalRandom.current().nextFloat() * total;
        int chosen = 0;
        for (int i = 0; i < weights.length; i++) {
            roll -= weights[i];
            if (roll <= 0.0f) { chosen = i; break; }
        }

        return validateCandidate(pool.get(chosen).type, pool, weights, chosen, level, pos, random);
    }

    private static @Nullable EntityType<?> validateCandidate(
            EntityType<?> first,
            List<WeightedType> pool,
            float[] weights,
            int firstIdx,
            ServerLevelAccessor level,
            BlockPos pos,
            RandomSource random
    ) {
        if (SpawnPlacements.checkSpawnRules(first, level, MobSpawnType.NATURAL, pos, random))
            return first;

        Integer[] order = new Integer[pool.size()];
        for (int i = 0; i < order.length; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> Float.compare(weights[b], weights[a]));

        int tried = 0;
        for (int idx : order) {
            if (idx == firstIdx) continue;
            if (weights[idx] <= 0) continue;
            if (++tried > 6) break;

            EntityType<?> cand = pool.get(idx).type;
            if (SpawnPlacements.checkSpawnRules(cand, level, MobSpawnType.NATURAL, pos, random))
                return cand;
        }
        return null;
    }

    private static boolean trySpawnReplacement(
            Mob original,
            EntityType<?> type,
            ServerLevelAccessor level,
            MobSpawnEvent.FinalizeSpawn event
    ) {
        Entity ent = type.create(level.getLevel());
        if (!(ent instanceof Mob replacement)) return false;

        replacement.moveTo(original.getX(), original.getY(), original.getZ(),
                original.getYRot(), original.getXRot());
        replacement.addTag("linggango_processed");

        ForgeEventFactory.onFinalizeSpawn(
                replacement, level,
                level.getCurrentDifficultyAt(replacement.blockPosition()),
                MobSpawnType.NATURAL, null, null
        );

        if (level.getLevel().addFreshEntity(replacement)) {
            original.discard();
            event.setSpawnCancelled(true);
            event.setCanceled(true);
            return true;
        }
        replacement.discard();
        return false;
    }

    private static synchronized ChunkSpawnData getChunkData(ChunkPos pos) {
        long key = ChunkPos.asLong(pos.x, pos.z);
        ChunkSpawnData data = CHUNK_DATA.get(key);
        long now = System.currentTimeMillis();
        if (data == null || (now - data.lastUpdate) > CHUNK_TTL_MS) {
            data = new ChunkSpawnData();
            CHUNK_DATA.put(key, data);
        }
        return data;
    }

    private static synchronized void cleanupOldChunks() {
        long now = System.currentTimeMillis();
        CHUNK_DATA.long2ObjectEntrySet()
                .removeIf(e -> (now - e.getValue().lastUpdate) > CHUNK_TTL_MS);
    }

    private static synchronized void initPools() {
        if (poolsInitialized) return;
        poolsInitialized = true;
        SpawnChanges.init();

        for (String typeId : SpawnChanges.TWEAKED_ENTITIES) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(typeId));
            if (type == null) continue;

            MobCategory cat = type.getCategory();
            boolean vanilla = isVanilla(type);
            float weight = vanilla ? 22.0f : 16.0f;

            switch (cat) {
                case CREATURE -> weight *= 1.2f;
                case AMBIENT  -> weight *= 1.6f;
                case WATER_CREATURE -> weight *= 1.3f;
                case MONSTER  -> weight *= 1.0f;
                default       -> weight *= 1.1f;
            }

            REPLACEMENT_POOLS.computeIfAbsent(cat, k -> new ArrayList<>())
                    .add(new WeightedType(type, weight, vanilla));
        }

        for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES.getValues()) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
            if (id == null || !id.getNamespace().equals("minecraft")) continue;

            MobCategory cat = type.getCategory();
            List<WeightedType> list = REPLACEMENT_POOLS.computeIfAbsent(cat, k -> new ArrayList<>());
            if (list.stream().noneMatch(w -> w.type == type)) {
                list.add(new WeightedType(type, 20.0f, true));
            }
        }

        REPLACEMENT_POOLS.values().removeIf(List::isEmpty);
    }

    private static void initBossBlacklist() {
        if (!BOSS_BLACKLIST.isEmpty()) return;
        BOSS_BLACKLIST.addAll(TweaksConfig.BOSS_MOB_BLACKLIST.get());
    }

    private static boolean isVanilla(EntityType<?> type) {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        return id != null && id.getNamespace().equals("minecraft");
    }

    private static class ChunkSpawnData {
        long lastUpdate = System.currentTimeMillis();
        final Object2IntOpenHashMap<EntityType<?>> counts = new Object2IntOpenHashMap<>();
        int vanillaCount = 0;
        int moddedCount = 0;
        int totalCount = 0;

        void record(EntityType<?> type, boolean vanilla) {
            counts.addTo(type, 1);
            totalCount++;
            if (vanilla) vanillaCount++;
            else moddedCount++;
            lastUpdate = System.currentTimeMillis();
        }

        int getCount(EntityType<?> type) {
            return counts.getOrDefault(type, 0);
        }

        float getModdedRatio() {
            return totalCount == 0 ? 0.0f : moddedCount / (float) totalCount;
        }
    }

    private record WeightedType(EntityType<?> type, float baseWeight, boolean isVanilla) {}
}