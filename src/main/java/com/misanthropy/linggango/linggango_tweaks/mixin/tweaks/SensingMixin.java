package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.world.entity.ai.sensing.Sensing;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Sensing.class)
public class SensingMixin {

    @Unique
    private static final Logger LINGGANGO_LOGGER = LogUtils.getLogger();

    @Unique
    private static boolean linggango$loggedInit = false;

    @Unique
    private int linggango$tickDelay = 0;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void linggango$cacheLOS(CallbackInfo ci) {
        if (!linggango$loggedInit) {
            LINGGANGO_LOGGER.info("[Linggango Tweaks] Line of Sight Caching active! Intercepted Sensing.class.");
            linggango$loggedInit = true;
        }

        linggango$tickDelay++;
        if (linggango$tickDelay % 3 != 0) {
            ci.cancel();
        }
    }
}