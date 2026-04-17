package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Unique private double linggango$smoothedFov = -1.0;
    @Unique private long linggango$lastNanoTime = -1;
    @Unique private long linggango$frameCounter = 0;
    @Unique private long linggango$lastCalculatedFrame = -1;

    @Inject(method = "render", at = @At("HEAD"))
    private void countPhysicalFrames(float partialTicks, long nanoTime, boolean renderLevel, CallbackInfo ci) {
        linggango$frameCounter++;
    }
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void smoothGlobalFov(Camera camera, float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Double> cir) {
        if (!useFOVSetting) return;

        double targetFov = cir.getReturnValueD();
        if (linggango$smoothedFov == -1.0 || Math.abs(linggango$smoothedFov - targetFov) > 40.0) {
            linggango$smoothedFov = targetFov;
            linggango$lastNanoTime = System.nanoTime();
            linggango$lastCalculatedFrame = linggango$frameCounter;
            return;
        }
        if (linggango$lastCalculatedFrame != linggango$frameCounter) {
            long currentNanoTime = System.nanoTime(); float deltaTime = (currentNanoTime - linggango$lastNanoTime) / 1_000_000_000.0F;

            if (deltaTime > 0) {
                if (deltaTime > 0.1F) deltaTime = 0.1F; float speed = 12.0F; double lerpFactor = 1.0 - Math.exp(-speed * deltaTime);

                linggango$smoothedFov = Mth.lerp(lerpFactor, linggango$smoothedFov, targetFov);
                if (Math.abs(linggango$smoothedFov - targetFov) < 0.01) { linggango$smoothedFov = targetFov;
                }

                linggango$lastNanoTime = currentNanoTime; linggango$lastCalculatedFrame = linggango$frameCounter;
            }
        }

        cir.setReturnValue(linggango$smoothedFov);
    }
}