package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;


import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.thecelestialworkshop.celestisynth.common.attack.breezebreaker.BreezebreakerWhirlwindAttack;
import org.thecelestialworkshop.celestisynth.common.entity.skillcast.SkillCastBreezebreakerTornado;

@Mixin(BreezebreakerWhirlwindAttack.class)
public class BreezebreakerWhirlwindDamageMixin {

    @Redirect(
            method = "tickAttack",
            at = @At(
                    value = "FIELD",
                    target = "Lorg/thecelestialworkshop/celestisynth/common/entity/skillcast/SkillCastBreezebreakerTornado;damage:F",
                    opcode = 181
            ),
            remap = false
    )
    private void buffTornadoDamage(@NonNull SkillCastBreezebreakerTornado tornado, float originalDamage) {
        tornado.damage = originalDamage * 5.0F;
    }
}