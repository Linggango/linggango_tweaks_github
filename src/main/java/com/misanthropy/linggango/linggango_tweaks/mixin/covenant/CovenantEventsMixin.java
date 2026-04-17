package com.misanthropy.linggango.linggango_tweaks.mixin.covenant;

import com.misanthropy.linggango.linggango_tweaks.chaos.ChaosDifficultyAddon;
import net.llenzzz.covenant_of_the_seven.events.CovenantEvents;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CovenantEvents.class)
public class CovenantEventsMixin {
    @Inject(method = "mutualDestruction", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cancelApocalypse(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        if (event.player != null) {
            Level level = event.player.level();
            if (ChaosDifficultyAddon.isChaos(level)) {
                ci.cancel();
            }
        }
    }
}