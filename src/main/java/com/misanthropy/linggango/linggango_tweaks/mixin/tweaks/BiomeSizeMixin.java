package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(MultiNoiseBiomeSource.class)
public class BiomeSizeMixin {

    @ModifyVariable(method = "getNoiseBiome*", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int linggango$modifyBiomeX(int x) {
        double multiplier = TweaksConfig.GLOBAL_BIOME_SIZE_MULTIPLIER.get();
        if (multiplier <= 0.1 || multiplier == 1.0) {
            return x;
        }
        return (int) Math.round(x / multiplier);
    }

    @ModifyVariable(method = "getNoiseBiome*", at = @At("HEAD"), ordinal = 2, argsOnly = true)
    private int linggango$modifyBiomeZ(int z) {
        double multiplier = TweaksConfig.GLOBAL_BIOME_SIZE_MULTIPLIER.get();
        if (multiplier <= 0.1 || multiplier == 1.0) {
            return z;
        }
        return (int) Math.round(z / multiplier);
    }
}