package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;

import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.thecelestialworkshop.celestisynth.common.attack.aquaflora.AquafloraBlastOffAttack;
import org.thecelestialworkshop.celestisynth.common.attack.aquaflora.AquafloraFlowersAwayAttack;
import org.thecelestialworkshop.celestisynth.common.attack.aquaflora.AquafloraPetalPiercesAttack;
import org.thecelestialworkshop.celestisynth.common.attack.aquaflora.AquafloraSlashFrenzyAttack;

@Mixin(value = {
        AquafloraBlastOffAttack.class,
        AquafloraFlowersAwayAttack.class,
        AquafloraPetalPiercesAttack.class,
        AquafloraSlashFrenzyAttack.class
})
public abstract class AquafloraCooldownMixin {

    @Inject(method = "getCooldown", at = @At("HEAD"), cancellable = true, remap = false)
    private void tweakAquafloraCooldowns(@NonNull CallbackInfoReturnable<Integer> cir) {
        Object self = this;

        if (self instanceof AquafloraBlastOffAttack) {
            cir.setReturnValue(60);
        } else if (self instanceof AquafloraFlowersAwayAttack) {
            cir.setReturnValue(200);
        } else if (self instanceof AquafloraPetalPiercesAttack) {
            cir.setReturnValue(60);
        } else if (self instanceof AquafloraSlashFrenzyAttack) {
            cir.setReturnValue(400);
        }
    }

    @Inject(method = "tickAttack", at = @At("HEAD"), cancellable = true, remap = false)
    private void reduceFencingHits(@NonNull CallbackInfo ci) {
        if ((Object) this instanceof AquafloraPetalPiercesAttack) {
            try {
                int t = (int) this.getClass().getMethod("getTimerProgress").invoke(this);
                if (t != 0 && t != 2 && t != 5 && t != 7 && t != 10 && t != 12 && t != 15) {
                    ci.cancel();
                }
            } catch (Exception ignored) {
            }
        }
    }
}