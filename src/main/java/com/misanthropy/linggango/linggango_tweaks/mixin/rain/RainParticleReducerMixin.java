package com.misanthropy.linggango.linggango_tweaks.mixin.rain;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class RainParticleReducerMixin {

    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void linggangoTweaks$reduceRainDrops(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, CallbackInfo ci) {
        if (particleData == ParticleTypes.RAIN) {
            ClientLevel level = (ClientLevel) (Object) this;
            boolean isThunder = level.isThundering();
            float cancelChance = isThunder ? 0.35f : 0.85f;

            if (Math.random() < cancelChance) {
                ci.cancel();
            }
        }
    }
}