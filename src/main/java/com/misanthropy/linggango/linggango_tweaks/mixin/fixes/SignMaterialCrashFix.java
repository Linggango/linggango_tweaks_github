package com.misanthropy.linggango.linggango_tweaks.mixin.fixes;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Sheets.class)
public class SignMaterialCrashFix {

    @Inject(method = "getSignMaterial", at = @At("RETURN"), cancellable = true)
    private static void linggango$ensureNonNullMaterial(WoodType woodType, @NonNull CallbackInfoReturnable<Material> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(Sheets.SIGN_MATERIALS.get(WoodType.OAK));
        }
    }

    @Inject(method = "getHangingSignMaterial", at = @At("RETURN"), cancellable = true)
    private static void linggango$ensureNonNullHangingMaterial(WoodType woodType, @NonNull CallbackInfoReturnable<Material> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(Sheets.HANGING_SIGN_MATERIALS.get(WoodType.OAK));
        }
    }
}