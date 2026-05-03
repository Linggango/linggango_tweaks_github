package com.misanthropy.linggango.linggango_tweaks.client.combat;

import com.misanthropy.linggango.linggango_tweaks.registry.SoundRegistry;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", value = Dist.CLIENT)
public class PerfectHitClient {

    private static final int MAX_FRAMES = 20;
    private static int hitStopFrames = 0;

    public static void triggerPerfectHit() {
        Minecraft mc = Minecraft.getInstance();
        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundRegistry.PERFECT_HIT.get(), 1.0F, 1.0F));
        hitStopFrames = MAX_FRAMES;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && hitStopFrames > 0) {
            hitStopFrames--;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        if (hitStopFrames > 0 && event.getOverlay().id().equals(VanillaGuiOverlay.VIGNETTE.id())) {
            GuiGraphics graphics = event.getGuiGraphics();
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();
            float progress = (float) hitStopFrames / MAX_FRAMES;

            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            int alpha = (int) (progress * 180);
            int color = (alpha << 24) | 0xFFFFFF;

            graphics.fill(0, 0, width, height, color);
            int edgeAlpha = (int) (progress * 100);
            graphics.renderOutline(0, 0, width, height, (edgeAlpha << 24) | 0xFFFFFF);

            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
        }
    }
}