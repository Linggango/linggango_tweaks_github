package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class FirstPersonPotionParticleMixin {
    @Inject(method = "tickEffects", at = @At("HEAD"), cancellable = true) private void hideFirstPersonParticles(@NonNull CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.level().isClientSide()) {
            if (entity.equals(Minecraft.getInstance().player) && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                ci.cancel();
            }
        }
    }
}