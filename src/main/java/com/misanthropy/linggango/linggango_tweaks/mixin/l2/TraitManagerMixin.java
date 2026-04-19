package com.misanthropy.linggango.linggango_tweaks.mixin.l2;

import com.misanthropy.linggango.linggango_tweaks.integration.l2.ApostleL2Data;
import dev.xkmc.l2hostility.content.logic.TraitManager;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = TraitManager.class, remap = false)
public class TraitManagerMixin {

    @Redirect(
            method = "fill",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"),
            remap = true)
    private static void skipHealthReset(@NonNull LivingEntity le, float health) {
        if (!ApostleL2Data.SKIP_HEALTH_RESET.contains(le.getUUID())) {
            le.setHealth(health);
        }
    }
}
