package com.misanthropy.linggango.linggango_tweaks.mixin.zeta;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(targets = {"org.violetmoon.zetaimplforge.event.ForgeZetaEventBus"}, remap = false)
public class ZetaLoadSafer {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "subscribeMethod", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false), remap = false, require = 1)
    private Object linggango$syncConvertedHandlerPut(Map handlers, Object key, Object value) {
        synchronized (handlers) {
            return handlers.put(key, value);
        }
    }

    @SuppressWarnings("rawtypes")
    @Redirect(method = "unsubscribeMethod", at = @At(value = "INVOKE", target = "Ljava/util/Map;remove(Ljava/lang/Object;)Ljava/lang/Object;", remap = false), remap = false, require = 1)
    private Object linggango$syncConvertedHandlerRemove(Map handlers, Object key) {
        synchronized (handlers) {
            return handlers.remove(key);
        }
    }
}