package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;


import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.thecelestialworkshop.celestisynth.common.attack.breezebreaker.*;

@Mixin(value = {
        BreezebreakerGalestormAttack.class,
        BreezebreakerDualGalestormAttack.class,
        BreezebreakerWheelAttack.class,
        BreezebreakerWhirlwindAttack.class,
        BreezebreakerWindRoarAttack.class
})
public abstract class BreezebreakerCooldownMixin {

    @Inject(method = "getCooldown", at = @At("HEAD"), cancellable = true, remap = false)
    private void tweakBreezebreakerCooldowns(@NonNull CallbackInfoReturnable<Integer> cir) {
        Object self = this;

        if (self instanceof BreezebreakerAttack attack) {
            if (self instanceof BreezebreakerGalestormAttack || self instanceof BreezebreakerDualGalestormAttack) {
                cir.setReturnValue(attack.buffStateModified(80));
            }
            else if (self instanceof BreezebreakerWhirlwindAttack) {
                cir.setReturnValue(attack.buffStateModified(120));
            }
            else if (self instanceof BreezebreakerWindRoarAttack) {
                cir.setReturnValue(attack.buffStateModified(60));
            }
            else if (self instanceof BreezebreakerWheelAttack) {
                cir.setReturnValue(attack.buffStateModified(80));
            }
        }
    }
}