package com.misanthropy.linggango.linggango_tweaks.mixin.tweaks;

import com.misanthropy.linggango.linggango_tweaks.tweaks.SpawnChanges;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.NaturalSpawner;
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
    private static void onCheckSpawnPosition(SpawnPlacements.Type placementType, LevelReader world, BlockPos pos, EntityType<?> entityType, CallbackInfoReturnable<Boolean> cir) {
        if (entityType == null) return;
        if (SpawnChanges.TWEAKED_ENTITIES.contains(entityType)) {
            if (ThreadLocalRandom.current().nextFloat() > 0.25f) {
                cir.setReturnValue(false);
                return;
            }
            if (entityType.getCategory() != MobCategory.WATER_CREATURE && entityType.getCategory() != MobCategory.WATER_AMBIENT) {
                if (world.getFluidState(pos).is(FluidTags.WATER) || world.getFluidState(pos.below()).is(FluidTags.WATER)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}