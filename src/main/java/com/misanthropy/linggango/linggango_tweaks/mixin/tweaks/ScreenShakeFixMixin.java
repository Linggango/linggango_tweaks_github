package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.ninni.species.client.screen.ScreenShakeEvent;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ScreenShakeEvent.class, remap = false)
public class ScreenShakeFixMixin {

    @Inject(method = "getDegree(Lnet/minecraft/world/entity/Entity;F)F", at = @At("HEAD"), cancellable = true)
    private void linggango$fixCameraNPE(@Nullable Entity cameraEntity, float partialTicks, @NonNull CallbackInfoReturnable<Float> cir) {
        if (cameraEntity == null) {
            cir.setReturnValue(0.0F);
        }
    }
}