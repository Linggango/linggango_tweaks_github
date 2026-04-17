package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class LetMeEatMixin {

    @Inject(method = "canEat", at = @At("HEAD"), cancellable = true)
    private void linggango$alwaysEat(boolean canAlwaysEat, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}