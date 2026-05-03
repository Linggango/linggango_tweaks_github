package com.misanthropy.linggango.linggango_tweaks.mixin.artifacts;

import artifacts.forge.client.ArmRenderHandler;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(value = ArmRenderHandler.class, remap = false)
public class ArtifactsArmRenderMixin {
    @Redirect(
            method = "lambda$onRenderArm$0",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/NonNullList;get(I)Ljava/lang/Object;"
            )
    )
    private static Object linggango$safeGetRenderBit(NonNullList<Boolean> list, int p_122791_) {
        if (p_122791_ >= list.size()) {
            return true;
        }
        return list.get(p_122791_);
    }
}