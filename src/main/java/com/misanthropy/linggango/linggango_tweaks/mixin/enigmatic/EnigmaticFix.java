package com.misanthropy.linggango.linggango_tweaks.mixin.enigmatic;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.aizistral.enigmaticlegacy.helpers.ItemNBTHelper", remap = false)
public class EnigmaticFix {

    @Inject(
            method = "verifyExistance",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void linggango$preventVerifyCrash(@Nullable ItemStack stack, String key, @NonNull CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || !stack.hasTag()) {
            cir.setReturnValue(false);
        }
    }
}