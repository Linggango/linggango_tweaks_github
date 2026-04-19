package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobLeashMixin {

    @Inject(method = "canBeLeashed", at = @At("HEAD"), cancellable = true)
    private void allowLeashingAnything(Player player, @NonNull CallbackInfoReturnable<Boolean> cir) {
        Mob self = (Mob) (Object) this;
        if (!self.isLeashed()) {
            cir.setReturnValue(true);
        }
    }
}