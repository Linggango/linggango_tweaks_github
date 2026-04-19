package com.misanthropy.linggango.linggango_tweaks.chaos;

import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class DifficultyRewardsHandler {

    @SubscribeEvent
    public static void onXpDrop(@NonNull LivingExperienceDropEvent event) {
        float multiplier = 1.0f;
        var level = event.getEntity().level();

        if (ChaosDifficultyAddon.isChaos(level)) {
            multiplier = 2.0f;
        }

        if (multiplier > 1.0f) {
            int newXp = Math.round(event.getDroppedExperience() * multiplier);
            event.setDroppedExperience(newXp);
        }
    }
}