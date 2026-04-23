package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.tweaks.SpawnChanges;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraftforge.registries.ForgeRegistries;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(NaturalSpawner.class)
public class SpawnChangesMixin {

    @Inject(
            method = "isSpawnPositionOk",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onCheckSpawnPosition(SpawnPlacements.Type p_47052_, @NonNull LevelReader p_47053_, @NonNull BlockPos p_47054_, @Nullable EntityType<?> p_47055_, @NonNull CallbackInfoReturnable<Boolean> cir) {
        if (p_47055_ == null) return;
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(p_47055_);

        if (id != null && SpawnChanges.TWEAKED_ENTITIES.contains(id.toString())) {
            if (ThreadLocalRandom.current().nextFloat() > 0.25f) {
                cir.setReturnValue(false);
                return;
            }
            if (p_47055_.getCategory() != MobCategory.WATER_CREATURE && p_47055_.getCategory() != MobCategory.WATER_AMBIENT) {
                if (p_47053_.getFluidState(p_47054_).is(FluidTags.WATER) || p_47053_.getFluidState(p_47054_.below()).is(FluidTags.WATER)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}