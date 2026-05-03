package com.misanthropy.linggango.linggango_tweaks.client.subtle_tweaks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DynamicCrosshairHandler {

    private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("minecraft", "textures/gui/icons.png");
    private static float currentAlpha = 1.0f;

    @SubscribeEvent
    public static void onRenderCrosshair(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.options.hideGui || mc.hitResult == null) return;
            event.setCanceled(true);
            boolean isTargeting = mc.hitResult.getType() != HitResult.Type.MISS;
            float targetAlpha = isTargeting ? 1.0f : 0.2f;
            currentAlpha += (targetAlpha - currentAlpha) * 0.15f;
            currentAlpha = Math.max(0.0f, Math.min(1.0f, currentAlpha));
            GuiGraphics graphics = event.getGuiGraphics();
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();
            int x = (width - 15) / 2;
            int y = (height - 15) / 2;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            graphics.setColor(1.0f, 1.0f, 1.0f, currentAlpha);
            graphics.blit(GUI_ICONS_LOCATION, x, y, 0, 0, 15, 15);
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

            RenderSystem.disableBlend();
        }
    }
}