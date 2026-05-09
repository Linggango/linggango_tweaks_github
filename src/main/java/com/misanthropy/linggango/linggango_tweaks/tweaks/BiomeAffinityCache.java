package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BiomeAffinityCache {
    private static final Map<EntityType<?>, Set<ResourceKey<Biome>>> NATIVE_BIOMES = new ConcurrentHashMap<>();

    public static void recordNative(EntityType<?> type, ResourceKey<Biome> biomeKey) {
        NATIVE_BIOMES.computeIfAbsent(type, k -> ConcurrentHashMap.newKeySet()).add(biomeKey);
    }

    public static boolean isNative(EntityType<?> type, Holder<Biome> biome) {
        Set<ResourceKey<Biome>> natives = NATIVE_BIOMES.get(type);
        if (natives == null) return false;
        return biome.unwrapKey().map(natives::contains).orElse(false);
    }

    public static void clear() {
        NATIVE_BIOMES.clear();
    }
}