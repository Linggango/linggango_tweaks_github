package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.thecelestialworkshop.celestisynth.common.attack.cresentia.CrescentiaBarrageAttack;
import org.thecelestialworkshop.celestisynth.common.attack.cresentia.CrescentiaDragonAttack;

@Mixin(value = {
        CrescentiaBarrageAttack.class,
        CrescentiaDragonAttack.class
})
public abstract class CrescentiaCooldownMixin {
    @Inject(method = "getCooldown", at = @At("HEAD"), cancellable = true, remap = false)
    private void tweakCrescentiaCooldowns(CallbackInfoReturnable<Integer> cir) {
        Object self = this;
        if (self instanceof CrescentiaBarrageAttack) {
            cir.setReturnValue(100);
        }
        else if (self instanceof CrescentiaDragonAttack) {
            cir.setReturnValue(200);
        }
    }
}