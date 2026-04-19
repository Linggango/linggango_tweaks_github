package com.misanthropy.linggango.linggango_tweaks.mixin.performance;

import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class ClientCollisionOptimizationMixin {

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