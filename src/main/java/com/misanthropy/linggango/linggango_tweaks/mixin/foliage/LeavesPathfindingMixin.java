package com.misanthropy.linggango.linggango_tweaks.mixin.foliage;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WalkNodeEvaluator.class)
public class LeavesPathfindingMixin {

    @Inject(method = "getBlockPathTypeStatic", at = @At("HEAD"), cancellable = true)
    private static void linggango$staticPathType(BlockGetter level, BlockPos.MutableBlockPos pos, CallbackInfoReturnable<BlockPathTypes> cir) {
        if (level.getBlockState(pos).is(BlockTags.LEAVES)) {
            cir.setReturnValue(BlockPathTypes.WALKABLE);
        }
    }

    @Inject(method = "getBlockPathType", at = @At("HEAD"), cancellable = true)
    private void linggango$instancePathType(BlockGetter level, int x, int y, int z, Mob mob, CallbackInfoReturnable<BlockPathTypes> cir) {
        if (level.getBlockState(new BlockPos(x, y, z)).is(BlockTags.LEAVES)) {
            cir.setReturnValue(BlockPathTypes.WALKABLE);
        }
    }
}