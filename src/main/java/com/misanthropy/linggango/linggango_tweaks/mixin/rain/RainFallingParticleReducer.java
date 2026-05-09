package com.misanthropy.linggango.linggango_tweaks.mixin.rain;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public class RainFallingParticleReducer {
    @Redirect(method = "renderSnowAndRain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;getRainLevel(F)F"))
    private float linggangoTweaks$reduceVisualRainSheets(ClientLevel instance, float partialTicks) {
        float originalLevel = instance.getRainLevel(partialTicks);
        if (instance.isThundering()) {
            return originalLevel * 0.75f;
        } else {
            return originalLevel * 0.30f;
        }
    }
}