package com.misanthropy.linggango.linggango_tweaks.penalties;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class XpDeathPenalty {

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.@NonNull Clone event) {
        if (event.isWasDeath()) {
            Player original = event.getOriginal();
            Player newPlayer = event.getEntity();

            int currentXp = getTotalXp(original);
            int keptXp = (int) (currentXp * 0.185f);

            int level = 0;
            int xp = keptXp;
            while (true) {
                int needed = (level >= 30) ? (62 + (level - 30) * 9) : ((level >= 15) ? (37 + (level - 15) * 5) : (7 + level * 2));
                if (xp < needed) {
                    newPlayer.experienceLevel = level;
                    newPlayer.experienceProgress = (float) xp / (float) needed;
                    newPlayer.totalExperience = keptXp;
                    break;
                }
                xp -= needed;
                level++;
            }
        }
    }

    private static int getTotalXp(@NonNull Player player) {
        int level = player.experienceLevel;
        int total;
        int needed;

        if (level >= 30) {
            total = (9 * level * level - 325 * level + 4440) >> 1;
            needed = 62 + (level - 30) * 9;
        } else if (level >= 16) {
            total = (5 * level * level - 81 * level + 720) >> 1;
            needed = 37 + (level - 15) * 5;
        } else {
            total = level * level + 6 * level;
            needed = 7 + level * 2;
        }

        return total + Math.round(player.experienceProgress * needed);
    }
}