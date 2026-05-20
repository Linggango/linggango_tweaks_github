package com.misanthropy.linggango.linggango_tweaks.util.addons; // Credits: https://www.curseforge.com/minecraft/mc-mods/celestweaks

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementHelper {

    public static void grantAdvancement(ServerPlayer player, String modid, String advancementId) {
        MinecraftServer server = player.getServer();
        if (server != null) {
            Advancement advancement = server.getAdvancements().getAdvancement(new ResourceLocation(modid, advancementId));
            if (advancement != null) {
                AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
                if (!progress.isDone()) {
                    for (String criterion : progress.getRemainingCriteria()) {
                        player.getAdvancements().award(advancement, criterion);
                    }
                }
            }
        }
    }
}