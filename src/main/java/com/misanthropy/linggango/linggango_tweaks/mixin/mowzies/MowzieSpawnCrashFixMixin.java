package com.misanthropy.linggango.linggango_tweaks.mixin.mowzies;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.bobmowzie.mowziesmobs.server.entity.MowzieEntity", remap = false)
public class MowzieSpawnCrashFixMixin {

    @Inject(
            method = "spawnPredicate",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void linggango$safeSpawnCheck(EntityType<?> type, LevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> cir) {
        if (!(world instanceof ServerLevel)) {
            cir.setReturnValue(false);
        }
    }
}