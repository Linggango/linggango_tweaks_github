package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.world.entity.EntityType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SpawnStatistics {
    private static final Map<EntityType<?>, AtomicLong> GLOBAL_COUNTS = new ConcurrentHashMap<>();
    private static final AtomicLong TOTAL_COUNT = new AtomicLong(0);

    private static volatile long lastDecayTime = System.currentTimeMillis();
    private static final long DECAY_INTERVAL_MS = 300_000L;
    private static final double DECAY_FACTOR = 0.5;

    public static void record(EntityType<?> type) {
        decayIfNeeded();
        GLOBAL_COUNTS.computeIfAbsent(type, k -> new AtomicLong()).incrementAndGet();
        TOTAL_COUNT.incrementAndGet();
    }

    public static double getRelativeFrequency(EntityType<?> type) {
        decayIfNeeded();
        long total = TOTAL_COUNT.get();
        if (total == 0) return 1.0;

        long typeCount = GLOBAL_COUNTS.getOrDefault(type, new AtomicLong()).get();
        int uniqueTypes = Math.max(1, GLOBAL_COUNTS.size());
        double expected = total / (double) uniqueTypes;
        return expected == 0 ? 1.0 : typeCount / expected;
    }

    private static void decayIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastDecayTime > DECAY_INTERVAL_MS) {
            synchronized (SpawnStatistics.class) {
                if (now - lastDecayTime > DECAY_INTERVAL_MS) {
                    GLOBAL_COUNTS.values().forEach(v ->
                            v.updateAndGet(old -> (long) (old * DECAY_FACTOR)));
                    TOTAL_COUNT.updateAndGet(old -> (long) (old * DECAY_FACTOR));
                    lastDecayTime = now;
                }
            }
        }
    }
}