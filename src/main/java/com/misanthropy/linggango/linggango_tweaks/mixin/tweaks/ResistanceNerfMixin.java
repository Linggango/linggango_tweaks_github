package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ResistanceNerfMixin {

    @Unique
    private int linggango$capturedNetAmplifier = -1;
    @ModifyVariable(
            method = "getDamageAfterMagicAbsorb",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0
    )
    private int captureAndCapResistance(int originalI) {
        int netAmp = (originalI / 5) - 1;
        this.linggango$capturedNetAmplifier = netAmp;
        if (netAmp >= 0) {
            return 0;
        }
        return originalI;
    }
    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"), cancellable = true)
    private void applyCustomResistanceScaling(DamageSource source, float damage, CallbackInfoReturnable<Float> cir) {
        if (this.linggango$capturedNetAmplifier >= 0 && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            float currentDamage = cir.getReturnValue();
            int level = this.linggango$capturedNetAmplifier + 1;
            double reduction;

            if (level <= 10) {
                reduction = 0.20 + (level - 1) * 0.04444;
            } else {
                reduction = 0.60 + (level - 10) * 0.0004;
            }

            float factor = (float) Math.max(0.0, 1.0 - reduction);
            cir.setReturnValue(currentDamage * factor);
        }
        this.linggango$capturedNetAmplifier = -1;
    }
}