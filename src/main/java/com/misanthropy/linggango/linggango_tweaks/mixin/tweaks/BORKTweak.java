package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.goo.brutality.item.weapon.sword.BladeOfTheRuinedKingSword", remap = false)
public abstract class BORKTweak {

    @ModifyConstant(
            method = "m_7579_",
            constant = @Constant(floatValue = 0.08F),
            remap = false
    )
    private float linggango$reduceHPDamage(float original) {
        return 0.01F;
    }
}