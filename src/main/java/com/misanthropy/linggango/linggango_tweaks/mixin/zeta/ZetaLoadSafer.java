package com.misanthropy.linggango.linggango_tweaks.mixin.zeta;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(targets = {"org.violetmoon.zetaimplforge.event.ForgeZetaEventBus"}, remap = false)
public class ZetaLoadSafer {

    @Final
    @Shadow @Mutable
    private Map<Object, Object> convertedHandlers;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private void linggango$upgradeToConcurrentMap(CallbackInfo ci) {
        this.convertedHandlers = new ConcurrentHashMap<>();
    }
}