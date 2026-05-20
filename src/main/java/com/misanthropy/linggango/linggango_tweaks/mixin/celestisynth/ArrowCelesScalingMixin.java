package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.thecelestialworkshop.celestisynth.common.entity.mob.misc.RainfallTurret;
import org.thecelestialworkshop.celestisynth.common.entity.projectile.RainfallArrow;
import org.thecelestialworkshop.celestisynth.common.registry.CSAttributes;

@Mixin(RainfallArrow.class)
public abstract class ArrowCelesScalingMixin {

    @Inject(
            method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void linggango$applyCelestialScaling(Level pLevel, LivingEntity pShooter, CallbackInfo ci) {
        RainfallArrow self = (RainfallArrow) (Object) this;
        LivingEntity source = null;

        if (pShooter instanceof Player player) {
            source = player;
        } else if (pShooter instanceof RainfallTurret turret) {
            source = turret.getOwner();
        }

        if (source != null) {
            AttributeInstance attr = source.getAttribute(CSAttributes.CELESTIAL_DAMAGE.get());
            if (attr != null) {
                double celestial = attr.getValue();
                double scaleFactor = 0.01;
                double newDamage = self.getBaseDamage() * (1.0 + celestial * scaleFactor);
                self.setBaseDamage(newDamage);
            }
        }
    }
}