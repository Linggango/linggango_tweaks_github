package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.thecelestialworkshop.celestisynth.common.entity.projectile.RainfallArrow;

@Mixin(RainfallArrow.class)
public abstract class ArrowDamageMixin {
    @ModifyArg(
            method = "hitEffect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            index = 1
    )
    private float linggango$scaleAoEDamage(float originalDamage) {
        RainfallArrow self = (RainfallArrow) (Object) this;
        return (float) (self.getBaseDamage() * 0.2F);
    }
}