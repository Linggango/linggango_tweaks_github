package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import net.minecraft.server.commands.LocateCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocateCommand.class)
public class LocateFixForSpreadStructuresMixin {

    @Inject(method = "dist", at = @At("HEAD"), cancellable = true)
    private static void linggango$fixDistanceFloatOverflow(int x1, int z1, int x2, int z2, CallbackInfoReturnable<Float> cir) {
        double d0 = (double)(x2 - x1);
        double d1 = (double)(z2 - z1);
        cir.setReturnValue((float)Math.hypot(d0, d1));
    }
}