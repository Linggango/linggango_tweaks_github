package com.misanthropy.linggango.linggango_tweaks.client;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import com.misanthropy.linggango.linggango_tweaks.config.DisplayClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EmbeddiumEvents {
    private static boolean hasSyncedFps = false;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init event) {
        if (!hasSyncedFps && event.getScreen() instanceof TitleScreen) {
            if (DisplayClientConfig.AUTO_FPS_SYNC.get()) {
                Minecraft mc = Minecraft.getInstance();
                int refreshRate = mc.getWindow().getRefreshRate();
                if (refreshRate >= 30) {
                    mc.options.framerateLimit().set(refreshRate);
                    mc.options.enableVsync().set(true);
                    mc.options.save();
                }
            }
            hasSyncedFps = true;
        }
    }
}