package com.misanthropy.linggango.linggango_tweaks.mixin.citadel;

import com.github.alexthe666.citadel.web.WebHelper;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Mixin(value = WebHelper.class, remap = false)
public class WebHelperMixin {
    @Unique
    private static final Logger linggango_tweaks$LOGGER = LogUtils.getLogger();

    @Inject(method = "getURLContents", at = @At("HEAD"), cancellable = true)
    private static void bypassGetURLContents(String urlString, String backupFileLoc, CallbackInfoReturnable<BufferedReader> cir) {
        try {
            InputStream stream = WebHelper.class.getClassLoader().getResourceAsStream(backupFileLoc);

            if (stream == null) {
                linggango_tweaks$LOGGER.warn("[Linggango Tweaks] Citadel update file not found at: {}", backupFileLoc);
                cir.setReturnValue(null);
                return;
            }

            cir.setReturnValue(new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
        } catch (Exception e) {
            linggango_tweaks$LOGGER.error("[Linggango Tweaks] Failed to redirect Citadel stream", e);
            cir.setReturnValue(null);
        }
    }
}