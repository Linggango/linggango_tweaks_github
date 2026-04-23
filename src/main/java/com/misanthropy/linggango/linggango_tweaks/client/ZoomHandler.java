package com.misanthropy.linggango.linggango_tweaks.client;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, value = Dist.CLIENT)
public class ZoomHandler {

    private static float lastZoomProgress = 0.0f;
    private static float currentZoomProgress = 0.0f;
    private static final float ZOOM_STEP = 0.12f;
    private static final float ZOOM_DIVISOR = 4.0f;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) return;

        lastZoomProgress = currentZoomProgress;
        boolean isKeyDown = KeyBindings.ZOOM_KEY.isDown();

        if (isKeyDown) {
            currentZoomProgress = Math.min(1.0f, currentZoomProgress + ZOOM_STEP);
        } else {
            currentZoomProgress = Math.max(0.0f, currentZoomProgress - ZOOM_STEP);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        float partialTicks = (float) event.getPartialTick();
        float smoothProgress = Mth.lerp(partialTicks, lastZoomProgress, currentZoomProgress);
        if (smoothProgress > 0) {
            float easedProgress = (float) (1.0 - Math.cos(smoothProgress * Math.PI * 0.5));

            double baseFov = event.getFOV();
            double targetFov = baseFov / ZOOM_DIVISOR;
            event.setFOV(Mth.lerp(easedProgress, baseFov, targetFov));
        }
    }
}