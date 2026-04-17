package com.misanthropy.linggango.linggango_tweaks.tweaks;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "linggango_tweaks", value = Dist.CLIENT)
public class HotbarTextMover {

    @SubscribeEvent
    public static void onPreRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.ITEM_NAME.id())) {
            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(0.0F, -15.0F, 0.0F);
        }
    }

    @SubscribeEvent
    public static void onPostRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.ITEM_NAME.id())) {
            event.getGuiGraphics().pose().popPose();
        }
    }
}