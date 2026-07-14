package com.misanthropy.linggango.linggango_tweaks.client.subtle_tweaks;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HealthSaturationHandler {

    private static float visualDarkness = 0.0f;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        float target = 0.0f;
        if (!player.isCreative() && !player.isSpectator()) {
            float healthPct = player.getHealth() / player.getMaxHealth();
            if (healthPct < 0.5f) {
                target = (0.5f - healthPct) * 0.6f;
            }
        }

        visualDarkness += (target - visualDarkness) * 0.05f;
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.VIGNETTE.id())) return;
        if (visualDarkness <= 0.01f) return;

        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        int alpha = (int) (visualDarkness * 255.0f);
        int color = (alpha << 24);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        event.getGuiGraphics().fill(0, 0, width, height, color);
        RenderSystem.disableBlend();
    }
}