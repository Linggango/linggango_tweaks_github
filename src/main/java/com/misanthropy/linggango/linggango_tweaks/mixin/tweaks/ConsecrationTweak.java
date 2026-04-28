package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.llenzzz.covenant_of_the_seven.events.item.virtue.ConsecrationEvents", remap = false)
public abstract class ConsecrationTweak {

    @Redirect(
            method = "onHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHurtEvent;setAmount(F)V", ordinal = 0),
            remap = false
    )
    private static void linggango$nerfConsecrationImmunity(LivingHurtEvent event, float amount) {
        event.setAmount(event.getAmount() * 0.50f);
    }

    @ModifyConstant(
            method = "onHurt",
            constant = @Constant(floatValue = 4.0F),
            remap = false
    )
    private static float linggango$reduceConsecrationPenalty(float original) {
        return 3.0F;
    }
}