package com.misanthropy.linggango.linggango_tweaks.mixin.performance;

import com.misanthropy.linggango.linggango_tweaks.config.SpawnerClientConfig;
import com.misanthropy.linggango.linggango_tweaks.config.TweaksConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class PerformanceTweaks {
    private PerformanceTweaks() {}
}

@Mixin(targets = "com.github.alexthe666.alexsmobs.event.ServerEvents", remap = false)
class AlexsMobsThrottleMixin {

    @Inject(method = "onLivingUpdateEvent", at = @At("HEAD"), cancellable = true, remap = false)
    private void throttleLivingUpdate(LivingEvent.@NonNull LivingTickEvent event, @NonNull CallbackInfo ci) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }

        if ((entity.tickCount & 3) != (entity.getId() & 3)) {
            ci.cancel();
        }
    }
}

@Mixin(CampfireBlock.class)
class CampfireBlockMixin {

    @Inject(
            method = "makeParticles",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onMakeParticles(Level level, BlockPos pos, boolean isSignalFire, boolean spawnExtraSmoke, CallbackInfo ci) {
        if (TweaksConfig.CAMPFIRE_SMOKE_ELIMINATION != null) {
            double eliminationChance = TweaksConfig.CAMPFIRE_SMOKE_ELIMINATION.get();
            if (eliminationChance >= 1.0) {
                ci.cancel();
            } else if (eliminationChance > 0.0 && level.random.nextFloat() <= eliminationChance) {
                ci.cancel();
            }
        }
    }
}

@Mixin(LivingEntity.class)
class ClientCollisionOptimizationMixin {

    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void linggango$noClientPushable(@NonNull CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "pushEntities", at = @At("HEAD"), cancellable = true)
    private void linggango$noClientPushEntities(@NonNull CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide) {
            ci.cancel();
        }
    }
}

@Mixin(SpawnerRenderer.class)
class SpawnerRenderMixin {

    @Redirect(
            method = "render(Lnet/minecraft/world/level/block/entity/SpawnerBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/BaseSpawner;getOrCreateDisplayEntity(Lnet/minecraft/world/level/Level;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/entity/Entity;")
    )
    private @Nullable Entity hideSpinningEntity(@NonNull BaseSpawner spawner, @NonNull Level level, @NonNull RandomSource random, @NonNull BlockPos pos) {
        if (SpawnerClientConfig.REQUIRE_SNEAK_FOR_ENTITY.get()) {
            Player player = Minecraft.getInstance().player;
            if (player == null || !player.isShiftKeyDown()) {
                return null;
            }
        }
        return spawner.getOrCreateDisplayEntity(level, random, pos);
    }
}