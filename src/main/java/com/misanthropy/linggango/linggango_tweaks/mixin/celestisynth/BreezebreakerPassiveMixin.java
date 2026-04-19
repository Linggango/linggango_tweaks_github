package com.misanthropy.linggango.linggango_tweaks.mixin.celestisynth;

import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.thecelestialworkshop.celestisynth.common.item.weapons.BreezebreakerItem;

@Mixin(BreezebreakerItem.class)
public class BreezebreakerPassiveMixin {

    @Redirect(
            method = "onPlayerHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V",
                    remap = false
            ),
            remap = false
    )
    private void nerfPassiveVulnerability(@NonNull LivingHurtEvent instance, float amount) {
        instance.setAmount(instance.getAmount() * 1.20F);
    }
}