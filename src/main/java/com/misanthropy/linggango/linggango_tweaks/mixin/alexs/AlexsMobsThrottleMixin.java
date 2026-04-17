package com.misanthropy.linggango.linggango_tweaks.mixin.alexs;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.github.alexthe666.alexsmobs.event.ServerEvents", remap = false)
public class AlexsMobsThrottleMixin {

    @Inject(method = "onLivingUpdateEvent", at = @At("HEAD"), cancellable = true, remap = false)
    private void throttleLivingUpdate(LivingEvent.LivingTickEvent event, CallbackInfo ci) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }

        if (entity.tickCount % 4 != Math.abs(entity.getId() % 4)) { ci.cancel();
        }
    }
}