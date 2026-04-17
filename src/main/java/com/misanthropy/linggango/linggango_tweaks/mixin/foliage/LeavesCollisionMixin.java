package com.misanthropy.linggango.linggango_tweaks.mixin.foliage;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LeavesCollisionMixin {

    @Unique
    private BlockBehaviour.BlockStateBase linggango$self() {
        return (BlockBehaviour.BlockStateBase) (Object) this;
    }

    @Inject(method = "getCollisionShape(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/shapes/CollisionContext;)Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
    private void linggango$getCollisionShape(BlockGetter level, BlockPos pos, CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (this.linggango$self().is(BlockTags.LEAVES)) {
            cir.setReturnValue(Shapes.empty());
        }
    }

    @Inject(method = "isSuffocating", at = @At("HEAD"), cancellable = true)
    private void linggango$isSuffocating(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (this.linggango$self().is(BlockTags.LEAVES)) {
            cir.setReturnValue(false);
        }
    }

    @Mixin(targets = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase$Cache")
    public static class CacheMixin {
        @Shadow @Final @Mutable protected boolean isCollisionShapeFullBlock;

        @Inject(method = "<init>", at = @At("RETURN"))
        private void linggango$modifyCache(BlockState state, CallbackInfo ci) {
            if (state.is(BlockTags.LEAVES)) {
                this.isCollisionShapeFullBlock = true;
            }
        }
    }
}