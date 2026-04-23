package com.misanthropy.linggango.linggango_tweaks.mixin.goety;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("removal")
@Mixin(targets = "com.Polarice3.Goety.utils.BlockFinder", remap = false)
public class GoetyBlockFinderPatchMixin {

        @Inject(method = "findIllagerWard*", at = @At("HEAD"), cancellable = true, remap = false)
    private static void patchIllagerWard(@NonNull ServerLevel serverLevel, @NonNull BlockPos blockPos, int soulEnergy, @NonNull CallbackInfoReturnable<Boolean> cir) {
        Player player = serverLevel.getNearestPlayer(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 32.0D, false);

        if (player != null) {
            MobEffect huntingDenial = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation("goetydelight", "hunting_denial"));

            if (huntingDenial != null && player.hasEffect(huntingDenial)) {
                cir.setReturnValue(true);
            }
        }
    }
}