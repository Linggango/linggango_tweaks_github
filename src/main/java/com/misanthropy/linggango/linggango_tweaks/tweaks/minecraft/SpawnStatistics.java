package com.misanthropy.linggango.linggango_tweaks.tweaks.minecraft;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SpawnStatistics {
    private static final Map<MobCategory, CategoryStats> STATS = new ConcurrentHashMap<>();

    private static class CategoryStats {
        final Map<EntityType<?>, AtomicLong> counts = new ConcurrentHashMap<>();
        final AtomicLong total = new AtomicLong(0);
    }

    private static volatile long lastDecayTime = System.currentTimeMillis();
    private static final long DECAY_INTERVAL_MS = 300_000L;
    private static final double DECAY_FACTOR = 0.5;

    public static void record(EntityType<?> type, MobCategory category) {
        if (category == null) return;
        decayIfNeeded();

        CategoryStats stats = STATS.computeIfAbsent(category, k -> new CategoryStats());
        stats.counts.computeIfAbsent(type, k -> new AtomicLong()).incrementAndGet();
        stats.total.incrementAndGet();
    }

    public static double getRelativeFrequency(EntityType<?> type, MobCategory category) {
        if (category == null) return 1.0;

        CategoryStats stats = STATS.get(category);
        if (stats == null) return 1.0;

        long total = stats.total.get();
        if (total == 0) return 1.0;

        long typeCount = stats.counts.getOrDefault(type, new AtomicLong(0)).get();
        int uniqueTypes = Math.max(1, stats.counts.size());
        double expected = total / (double) uniqueTypes;

        return expected == 0 ? 1.0 : typeCount / expected;
    }

    private static void decayIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastDecayTime > DECAY_INTERVAL_MS) {
            synchronized (SpawnStatistics.class) {
                if (now - lastDecayTime > DECAY_INTERVAL_MS) {
                    lastDecayTime = now;
                    STATS.values().forEach(stats -> {
                        stats.counts.values().forEach(v -> v.updateAndGet(old -> (long) (old * DECAY_FACTOR)));
                        stats.total.updateAndGet(old -> (long) (old * DECAY_FACTOR));
                    });
                }
            }
        }
    }
}