package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.world.entity.ai.goal.TemptGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TemptGoal.class)
public class TemptDelayMixin {
    @Shadow(aliases = {"f_25925_", "calmDown"})
    private int calmDown;

    @Inject(method = "canUse", at = @At("HEAD"))
    private void removeTemptDelay(CallbackInfoReturnable<Boolean> cir) {
        this.calmDown = 0;
    }
}