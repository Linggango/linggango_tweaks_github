package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.goo.brutality.client.renderers.layers.EyeOfViolenceLayer", remap = false)
public abstract class EyeForViolenceTweakMixin {

    @ModifyConstant(
            method = "render*",
            constant = @Constant(floatValue = 25.0F),
            remap = false
    )
    private float linggango$reduceEyeRange(float original) {
        return 5.0F;
    }
}