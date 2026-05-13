package com.misanthropy.linggango.linggango_tweaks.tweaks;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class SpawnChanges implements BiomeModifier {
    public static final SpawnChanges INSTANCE = new SpawnChanges();
    public static final Set<String> TWEAKED_ENTITIES = new HashSet<>();
    private static final Set<ModifiableBiomeInfo.BiomeInfo.Builder> PROCESSED_BUILDERS = Collections.newSetFromMap(new WeakHashMap<>());

    public static final Set<String> DISABLED_ENTITIES = Set.of(
            "terra_entity:green_slime"
    );

    public static final Set<String> BOSS_BLACKLIST = Set.of(
            "minecraft:wither", "minecraft:ender_dragon", "minecraft:warden",
            "cataclysm:ender_golem", "cataclysm:ignis", "cataclysm:netherite_monstrosity",
            "cataclysm:the_harbinger", "cataclysm:the_leviathan", "cataclysm:ancient_remnant"
    );

    public static void clear() {
        TWEAKED_ENTITIES.clear();
    }

    public static boolean isBlocked(ResourceLocation id) {
        String full = id.toString();
        if (DISABLED_ENTITIES.contains(full) || BOSS_BLACKLIST.contains(full)) return true;

        List<? extends String> configBlacklist = TweaksConfig.BOSS_MOB_BLACKLIST.get();
        if (configBlacklist != null && configBlacklist.contains(full)) return true;

        String path = id.getPath().toLowerCase(java.util.Locale.ROOT);
        List<? extends String> autoExclude = TweaksConfig.AUTO_EXCLUDED_MOB_SEGMENTS.get();

        if (autoExclude != null) {
            for (String seg : path.split("_")) {
                if (autoExclude.contains(seg)) return true;
            }
        }
        return false;
    }

    @Override
    public void modify(@NonNull Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.@NonNull Builder builder) {
        if (phase != Phase.MODIFY || !PROCESSED_BUILDERS.add(builder)) return;

        ResourceKey<Biome> biomeKey = biome.unwrapKey().orElse(null);

        for (MobCategory category : MobCategory.values()) {
            List<MobSpawnSettings.SpawnerData> spawners = builder.getMobSpawnSettings().getSpawner(category);
            if (spawners == null || spawners.isEmpty()) continue;

            if (biomeKey != null) {
                for (MobSpawnSettings.SpawnerData data : spawners) {
                    ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(data.type);
                    if (id == null || isBlocked(id) || id.getNamespace().equals("macabre")) continue;
                    TWEAKED_ENTITIES.add(id.toString());
                    BiomeAffinityCache.recordNative(data.type, biomeKey);
                }
            }

            List<MobSpawnSettings.SpawnerData> newSpawns = new ArrayList<>();
            double globalMult = Math.max(0.1, Math.min(5.0, TweaksConfig.GLOBAL_MOB_MULTIPLIER.get()));

            for (MobSpawnSettings.SpawnerData data : spawners) {
                ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(data.type);
                if (id == null || isBlocked(id)) continue;

                if (id.getNamespace().equals("macabre")) {
                    newSpawns.add(data);
                    continue;
                }

                int weight = applyStaticMultipliers(id, data.getWeight().asInt(), category, biome);
                weight = Math.max(1, (int) (weight * globalMult));
                newSpawns.add(new MobSpawnSettings.SpawnerData(data.type, weight, data.minCount, data.maxCount));
            }
            spawners.clear();
            spawners.addAll(newSpawns);
        }
    }

    private int applyStaticMultipliers(ResourceLocation id, int weight, MobCategory category, Holder<Biome> biome) {
        String full = id.toString();
        String ns = id.getNamespace();

        return switch (ns) {
            case "alexsmobs" -> {
                if ("alexsmobs:rhinoceros".equals(full)) yield Math.max(1, (int) (weight * TweaksConfig.ALEXSMOBS_RHINO_MULTIPLIER.get()));
                if ("alexsmobs:flying_fish".equals(full)) yield Math.max(1, weight / 6);
                yield isWaterCategory(category) ? Math.max(1, weight / 6) : weight;
            }
            case "monsterexpansion" -> Math.max(1, (int) (weight * TweaksConfig.MONSTEREXPANSION_MULTIPLIER.get()));
            case "saintsdragons" -> Math.max(1, (int) (weight * TweaksConfig.SAINTSDRAGONS_MULTIPLIER.get()));
            case "goetyhostility" -> Math.max(1, (int) (weight * TweaksConfig.GOETYHOSTILITY_MULTIPLIER.get()));
            case "terra_entity" -> {
                if (full.contains("slime")) yield Math.max(1, (int) (weight * TweaksConfig.TERRA_ENTITY_SLIME_MULTIPLIER.get()));
                if (biome.is(BiomeTags.IS_OVERWORLD)) yield Math.max(1, (int) (weight * TweaksConfig.TERRA_ENTITY_OVERWORLD_MULTIPLIER.get()));
                yield Math.max(1, weight / 2);
            }
            case "born_in_chaos_v1" -> Math.max(1, weight / 4);
            default -> weight;
        };
    }

    private static boolean isWaterCategory(MobCategory category) {
        return category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT || category == MobCategory.UNDERGROUND_WATER_CREATURE;
    }

    @Override
    public @NonNull Codec<? extends BiomeModifier> codec() {
        return LinggangoTweaks.SPAWN_CHANGES_CODEC.get();
    }
}