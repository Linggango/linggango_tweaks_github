package com.misanthropy.linggango.linggango_tweaks.mixin.balancing.covenant;

import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.aizistral.enigmaticlegacy.handlers.EnigmaticEventHandler", remap = false)
public abstract class CursedScrollTweak {

    @ModifyVariable(
            method = "onEntityHurt",
            at = @At(value = "STORE"),
            ordinal = 0,
            remap = false
    )
    private float linggango$capCursedScrollDamage(float damageBoost, LivingHurtEvent event) {
        float originalDamage = event.getAmount();
        float maxBonus = originalDamage * 0.15f;

        return Math.min(damageBoost, maxBonus);
    }

    @ModifyVariable(
            method = "miningStuff",
            at = @At(value = "STORE"),
            remap = false,
            name = "miningBoost")
    private float linggango$capCursedScrollMining(float miningBoost) {
        float additiveBoost = miningBoost - 1.0f;
        return 1.0f + Math.min(additiveBoost, 0.20f);
    }

    @Redirect(
            method = "onLivingHeal",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/entity/living/LivingHealEvent;setAmount(F)V"),
            remap = false
    )
    private void linggango$capCursedScrollRegen(LivingHealEvent event, float amount) {
        float originalHeal = event.getAmount();
        float proposedBonus = amount - originalHeal;
        float maxBonus = originalHeal * 0.06f;

        event.setAmount(originalHeal + Math.min(proposedBonus, maxBonus));
    }
}