package com.misanthropy.linggango.linggango_tweaks.mixin.enigmatic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(targets = "com.aizistral.enigmaticlegacy.brewing.AbstractBrewingRecipe", remap = false)
public class EnigmaticBrewingCrashFixMixin {

    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z",
                    remap = false
            ),
            require = 0
    )
    private boolean bypassNullListCrash(List<Object> list, Object recipe) {
        if (list == null) {
            return false;
        }
        return list.add(recipe);
    }
}