package com.misanthropy.linggango.linggango_tweaks.mixin.foliage;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class LeavesInteractionMixin {

    @Unique
    private BlockState self() {
        return (BlockState) (Object) this;
    }

    @Inject(method = "entityInside", at = @At("HEAD"))
    private void linggango$onEntityInside(Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (!this.self().is(BlockTags.LEAVES)) return;
        if (!(entity instanceof LivingEntity living)) return;
        float horizontalSpeed = 0.90F;
        float verticalSpeed = 0.85F;

        Vec3 motion = entity.getDeltaMovement();
        if (motion.y <= 0) {
            entity.setDeltaMovement(motion.multiply(horizontalSpeed, verticalSpeed, horizontalSpeed));
        }
        if (living.fallDistance > 2.0F) {
            living.fallDistance *= 0.5F;
        }
        if (level.isClientSide && entity.tickCount % 12 == 0) {
            double distMoved = entity.position().distanceToSqr(entity.xOld, entity.yOld, entity.zOld);
            if (distMoved > 0.001) {
                float pitch = 0.8F + level.random.nextFloat() * 0.4F;
                float volume = 0.1F + level.random.nextFloat() * 0.1F;
                SoundEvent sound = level.random.nextBoolean() ?
                        this.self().getSoundType().getFallSound() :
                        this.self().getSoundType().getHitSound();
                entity.playSound(sound, volume, pitch);
            }
        }
    }
}