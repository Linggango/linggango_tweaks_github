package com.misanthropy.linggango.linggango_tweaks.difficulty;

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

            newPlayer.experienceLevel = 0;
            newPlayer.experienceProgress = 0.0F;
            newPlayer.totalExperience = 0;
            newPlayer.giveExperiencePoints(keptXp);
        }
    }

    private static int getTotalXp(@NonNull Player player) {
        int level = player.experienceLevel;
        int total;
        if (level >= 30) {
            total = (int) (4.5 * level * level - 162.5 * level + 2220);
        } else if (level >= 16) {
            total = (int) (2.5 * level * level - 40.5 * level + 360);
        } else {
            total = level * level + 6 * level;
        }
        return total + Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
    }
}