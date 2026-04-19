package com.misanthropy.linggango.linggango_tweaks.mixin;

import net.mcreator.borninchaosv.procedures.BadFeelingactivationProcedure;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelAccessor;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BadFeelingactivationProcedure.class)
public class BadFeelingactivationProcedureMixin {

    @Inject(method = "execute", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cancelBadFeeling(LevelAccessor world, double x, double y, double z, Entity entity, @NonNull CallbackInfo ci) {
        ci.cancel();
    }
}