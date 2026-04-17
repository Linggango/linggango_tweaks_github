package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.server.packs.repository.PackCompatibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackCompatibility.class)
public class ResourcePackCompatWarningRemoverMixin {
    @Inject(method = "isCompatible", at = @At("HEAD"), cancellable = true)
    private void forceCompatibility(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}