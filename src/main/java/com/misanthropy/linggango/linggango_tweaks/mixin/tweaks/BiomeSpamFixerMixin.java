package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.biome.Climate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(Climate.ParameterList.class)
public class BiomeSpamFixerMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
    private static List<Pair<Climate.ParameterPoint, Object>> linggango$fixBiomeSpam(List<Pair<Climate.ParameterPoint, Object>> values) {
        if (values == null || values.isEmpty()) return values;

        Object firstItem = values.get(0).getSecond();
        if (firstItem == null || (!firstItem.toString().contains("biome") && !firstItem.toString().contains("Biome"))) {
            return values;
        }

        String fallbackNamespace = TweaksConfig.BIOME_FALLBACK_NAMESPACE.get();
        Map<String, Float> reductions = TweaksConfig.parsedBiomeReductions;

        if (reductions.isEmpty() || fallbackNamespace.isEmpty()) return values;

        List<Pair<Climate.ParameterPoint, Object>> newValues = new ArrayList<>();
        List<Object> fallbacks = new ArrayList<>();

        for (Pair<Climate.ParameterPoint, Object> pair : values) {
            String name = pair.getSecond().toString();
            if (name.contains(fallbackNamespace)) {
                boolean isReduced = false;
                for (String reducedBiome : reductions.keySet()) {
                    if (name.contains(reducedBiome)) {
                        isReduced = true;
                        break;
                    }
                }

                if (!isReduced) {
                    fallbacks.add(pair.getSecond());
                }
            }
        }

        Random rand = new Random(42L);

        for (Pair<Climate.ParameterPoint, Object> pair : values) {
            String name = pair.getSecond().toString();
            Object fallback = fallbacks.isEmpty() ? values.get(0).getSecond() : fallbacks.get(rand.nextInt(fallbacks.size()));

            boolean replaced = false;

            for (Map.Entry<String, Float> entry : reductions.entrySet()) {
                if (name.contains(entry.getKey())) {
                    if (rand.nextFloat() < entry.getValue()) {
                        newValues.add(Pair.of(pair.getFirst(), fallback));
                        replaced = true;
                    }
                    break;
                }
            }

            if (!replaced) {
                newValues.add(pair);
            }
        }

        return newValues;
    }
}