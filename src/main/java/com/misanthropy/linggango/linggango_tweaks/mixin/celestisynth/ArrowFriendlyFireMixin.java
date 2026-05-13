package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.thecelestialworkshop.celestisynth.common.entity.mob.misc.RainfallTurret;
import org.thecelestialworkshop.celestisynth.common.entity.projectile.RainfallArrow;

@Mixin(RainfallArrow.class)
public abstract class ArrowFriendlyFireMixin {

    @Shadow(remap = false)
    public RainfallTurret turretSource;

    @Unique
    private double linggango$originalDamage = -1.0;

    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void linggango$preOwnerHit(EntityHitResult hit, CallbackInfo ci) {
        RainfallArrow arrow = (RainfallArrow) (Object) this;

        if (this.linggango$originalDamage < 0.0) {
            this.linggango$originalDamage = arrow.getBaseDamage();
        }

        if (this.turretSource != null) {
            Entity target = hit.getEntity();
            if (target instanceof LivingEntity living) {
                Entity turretOwner = this.turretSource.getOwner();
                if (turretOwner != null && turretOwner == living) {
                    arrow.setBaseDamage(0.0);
                }
            }
        }
    }

    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void linggango$postOwnerHit(EntityHitResult hit, CallbackInfo ci) {
        RainfallArrow arrow = (RainfallArrow) (Object) this;
        if (this.turretSource != null && this.linggango$originalDamage >= 0.0) {
            arrow.setBaseDamage(this.linggango$originalDamage);
        }
    }
}