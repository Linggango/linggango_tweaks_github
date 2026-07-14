package com.misanthropy.linggango.linggango_tweaks.chaos;

import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jspecify.annotations.NonNull;

@Mod.EventBusSubscriber(modid = "linggango_tweaks")
public class DifficultyRewardsHandler {

    @SubscribeEvent
    public static void onXpDrop(@NonNull LivingExperienceDropEvent event) {
        if (ChaosDifficultyAddon.isChaos(event.getEntity().level())) {
            event.setDroppedExperience(event.getDroppedExperience() * 2);
        }
    }
}