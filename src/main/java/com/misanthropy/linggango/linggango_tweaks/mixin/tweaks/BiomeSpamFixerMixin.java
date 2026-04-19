package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.biome.Climate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(Climate.ParameterList.class)
public class BiomeSpamFixerMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static List<Pair<Climate.ParameterPoint, Object>> linggango$fixBiomeSpam(List<Pair<Climate.ParameterPoint, Object>> values) {
        if (values == null || values.isEmpty()) return values;

        Object firstItem = values.get(0).getSecond();
        if (firstItem == null || (!firstItem.toString().contains("biome") && !firstItem.toString().contains("Biome"))) {
            return values;
        }

        Map<String, Float> reductions = TweaksConfig.parsedBiomeReductions;
        if (reductions.isEmpty() || TweaksConfig.BIOME_FALLBACK_NAMESPACE.get().isEmpty()) {
            return values;
        }

        Map<String, Integer> biomeNodeCounts = new HashMap<>();
        for (Pair<Climate.ParameterPoint, Object> pair : values) {
            String name = pair.getSecond().toString();
            for (String reducedBiome : reductions.keySet()) {
                if (name.contains(reducedBiome)) {
                    biomeNodeCounts.put(reducedBiome, biomeNodeCounts.getOrDefault(reducedBiome, 0) + 1);
                    break;
                }
            }
        }

        List<Pair<Climate.ParameterPoint, Object>> newValues = new ArrayList<>();
        Map<String, Integer> nodesProcessed = new HashMap<>();

        for (Pair<Climate.ParameterPoint, Object> pair : values) {
            String name = pair.getSecond().toString();
            boolean isReduced = false;
            String matchedRule = null;

            for (String reducedBiome : reductions.keySet()) {
                if (name.contains(reducedBiome)) {
                    isReduced = true;
                    matchedRule = reducedBiome;
                    break;
                }
            }

            if (isReduced) {
                float reductionChance = reductions.get(matchedRule);
                int totalNodes = biomeNodeCounts.get(matchedRule);
                int nodesToKeep = Math.max(0, Math.round(totalNodes * (1.0f - reductionChance)));
                int processed = nodesProcessed.getOrDefault(matchedRule, 0);

                if (processed < nodesToKeep) {
                    newValues.add(pair);
                }

                nodesProcessed.put(matchedRule, processed + 1);
            } else {
                newValues.add(pair);
            }
        }

        return newValues;
    }
}