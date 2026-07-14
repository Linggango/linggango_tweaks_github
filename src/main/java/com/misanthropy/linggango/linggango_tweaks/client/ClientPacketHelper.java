package com.misanthropy.linggango.linggango_tweaks.client;

import com.misanthropy.linggango.linggango_tweaks.client.screen.ModernCreditsScreen;
import com.misanthropy.linggango.linggango_tweaks.config.LinggangoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;

public class ClientPacketHelper {

    public static void handlePlayCredits() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            net.minecraft.nbt.CompoundTag persistentData = mc.player.getPersistentData();
            net.minecraft.nbt.CompoundTag modData = persistentData.getCompound(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG);
            modData.putBoolean("LinggangoHasSeenCredits", true);
            persistentData.put(net.minecraft.world.entity.player.Player.PERSISTED_NBT_TAG, modData);
        }
        mc.setScreen(new ModernCreditsScreen());
    }

    public static void handleSyncExtras(boolean unlocked) {
        LinggangoConfig.setEnabled(unlocked);
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof PauseScreen) {
            mc.setScreen(mc.screen);
        }
    }

    public static void openRingScreen() {
        try {
            com.misanthropy.linggango.linggango_tweaks.ring_selection.ClientAccess.openScreen();
        } catch (Exception ignored) {
        }
    }
}