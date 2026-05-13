package com.misanthropy.linggango.linggango_tweaks.util.addons; // Credits: https://www.curseforge.com/minecraft/mc-mods/celestweaks

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public class HeartOfCreationPity {
    private static final String NBT_KEY = "HeartOfCreationPulls";

    public static int getPullNumber(Player player) {
        CompoundTag data = player.getPersistentData();
        return data.getInt(NBT_KEY);
    }

    public static void incrementPull(Player player) {
        CompoundTag data = player.getPersistentData();
        int current = data.getInt(NBT_KEY);
        data.putInt(NBT_KEY, current + 1);
    }

    public static void resetPulls(Player player) {
        player.getPersistentData().putInt(NBT_KEY, 0);
    }

    public static void setPulls(Player player, int pulls) {
        player.getPersistentData().putInt(NBT_KEY, Math.max(0, pulls));
    }

    public static double getCurrentChance(Player player, int realityRecompense) {
        int pull = getPullNumber(player) + 1;
        realityRecompense *= 10;

        if (pull >= 90 - realityRecompense) {
            return 1.0;
        } else if (pull >= 74 - realityRecompense) {
            return 0.066 + 0.06 * (pull - (74 - realityRecompense));
        } else {
            return 0.006;
        }
    }
}