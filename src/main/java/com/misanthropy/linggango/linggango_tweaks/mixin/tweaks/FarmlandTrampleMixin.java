package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmBlock.class)
public class FarmlandTrampleMixin {

    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void disableAllTrampling(Level level, BlockState state, BlockPos pos, Entity entity, float distance, CallbackInfo ci) {
        entity.causeFallDamage(distance, 0.0F, level.damageSources().fall());
        ci.cancel();
    }
}