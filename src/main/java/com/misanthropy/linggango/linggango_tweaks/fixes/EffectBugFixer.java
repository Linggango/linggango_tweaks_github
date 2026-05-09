package com.misanthropy.linggango.linggango_tweaks.fixes;

import com.misanthropy.linggango.linggango_tweaks.LinggangoTweaks;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LinggangoTweaks.MOD_ID)
public class EffectBugFixer {

    private static final int MAX_SAFE_DURATION = 19200;

    @SubscribeEvent
    public static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player) {
            int duration = event.getEffectInstance().getDuration();
            if (duration > MAX_SAFE_DURATION || duration == -1) {
                event.setResult(Event.Result.DENY);
            }
        }
    }
}