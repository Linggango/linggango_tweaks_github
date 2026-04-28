package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(Climate.ParameterList.class)
public class BiomeSpamFixerMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static List<Pair<Climate.ParameterPoint, Object>> linggango$fixBiomeSpam(@Nullable List<Pair<Climate.ParameterPoint, Object>> values) {
        if (values == null || values.isEmpty()) return values;
        Object firstItem = values.get(0).getSecond();
        if (!(firstItem instanceof ResourceKey<?> key) || !key.registry().equals(Registries.BIOME)) {
            return values;
        }

        Map<String, Float> reductions = TweaksConfig.parsedBiomeReductions;
        if (reductions == null || reductions.isEmpty()) return values;
        List<Pair<Climate.ParameterPoint, Object>> safeNodes = new ArrayList<>();
        Map<String, Integer> biomeNodeCounts = new HashMap<>();
        Map<Pair<Climate.ParameterPoint, Object>, String> nodeToRule = new HashMap<>();

        for (Pair<Climate.ParameterPoint, Object> pair : values) {
            String name = pair.getSecond().toString();
            String matchedRule = null;
            for (String rule : reductions.keySet()) {
                if (name.equals(rule) || name.endsWith(":" + rule)) {
                    matchedRule = rule;
                    break;
                }
            }

            if (matchedRule != null) {
                biomeNodeCounts.put(matchedRule, biomeNodeCounts.getOrDefault(matchedRule, 0) + 1);
                nodeToRule.put(pair, matchedRule);
            } else {
                safeNodes.add(pair);
            }
        }

        if (safeNodes.isEmpty()) return values;
        List<Pair<Climate.ParameterPoint, Object>> newValues = new ArrayList<>();
        Map<String, Integer> nodesProcessed = new HashMap<>();
        Map<Climate.ParameterPoint, Pair<Climate.ParameterPoint, Object>> closestCache = new HashMap<>();

        for (Pair<Climate.ParameterPoint, Object> pair : values) {
            String matchedRule = nodeToRule.get(pair);

            if (matchedRule != null) {
                float reductionChance = reductions.get(matchedRule);
                int totalNodes = biomeNodeCounts.get(matchedRule);
                int nodesToKeep = Math.round(totalNodes * (1.0f - reductionChance));
                int processed = nodesProcessed.getOrDefault(matchedRule, 0);

                if (processed < nodesToKeep) {
                    newValues.add(pair);
                } else {
                    newValues.add(closestCache.computeIfAbsent(pair.getFirst(),
                            p -> linggango$findClosestNeighborEntry(p, safeNodes)));
                }
                nodesProcessed.put(matchedRule, processed + 1);
            } else {
                newValues.add(pair);
            }
        }

        return newValues;
    }

    @Unique
    private static Pair<Climate.ParameterPoint, Object> linggango$findClosestNeighborEntry(Climate.ParameterPoint target, List<Pair<Climate.ParameterPoint, Object>> safeNodes) {
        Pair<Climate.ParameterPoint, Object> closestEntry = safeNodes.get(0);
        long minDistance = Long.MAX_VALUE;
        long tTemp = linggango$getMid(target.temperature());
        long tHum = linggango$getMid(target.humidity());
        long tCont = linggango$getMid(target.continentalness());
        long tEro = linggango$getMid(target.erosion());
        long tDep = linggango$getMid(target.depth());
        long tWei = linggango$getMid(target.weirdness());

        for (Pair<Climate.ParameterPoint, Object> safe : safeNodes) {
            Climate.ParameterPoint p = safe.getFirst();

            long dist = Mth.square(p.temperature().distance(tTemp)) +
                    Mth.square(p.humidity().distance(tHum)) +
                    Mth.square(p.continentalness().distance(tCont)) +
                    Mth.square(p.erosion().distance(tEro)) +
                    Mth.square(p.depth().distance(tDep)) +
                    Mth.square(p.weirdness().distance(tWei)) +
                    Mth.square(p.offset());

            if (dist < minDistance) {
                minDistance = dist;
                closestEntry = safe;
            }
        }
        return closestEntry;
    }

    @Unique
    private static long linggango$getMid(Climate.Parameter p) {
        return (p.min() + p.max()) / 2;
    }
}