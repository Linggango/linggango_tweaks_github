package com.misanthropy.linggango.linggango_tweaks.mixin.performance;

import com.misanthropy.linggango.linggango_tweaks.client.culling.BedrockCullingLogic;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BlockOcclusionCache.class, remap = false)
public abstract class BlockOcclusionCacheMixin {

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private void onShouldDrawSide(BlockState selfState, BlockGetter view, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> cir) {
        if (BedrockCullingLogic.shouldCull(facing, pos.getY())) {
            cir.setReturnValue(false);
        }
    }
}