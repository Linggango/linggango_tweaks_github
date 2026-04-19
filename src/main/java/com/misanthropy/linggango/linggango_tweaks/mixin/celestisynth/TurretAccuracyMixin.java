package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.thecelestialworkshop.celestisynth.common.entity.mob.misc.RainfallTurret;

@Mixin({RainfallTurret.class}) public abstract class TurretAccuracyMixin {
    @ModifyArg( method = {"tickShooting"},
            at = @At( value = "INVOKE", target = "Lorg/thecelestialworkshop/celestisynth/common/entity/projectile/RainfallArrow;shoot(DDDFF)V" ), index = 4 ) private float removeArrowInaccuracy(float inaccuracy) { return 0.0F;
    }
}
