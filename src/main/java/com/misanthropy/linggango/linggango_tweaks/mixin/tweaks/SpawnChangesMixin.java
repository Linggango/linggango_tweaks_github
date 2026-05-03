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

@Mixin(NaturalSpawner.class)
public class SpawnChangesMixin {

    @Inject(method = "isSpawnPositionOk", at = @At("HEAD"), cancellable = true)
    private static void onCheckSpawnPosition(
            SpawnPlacements.Type placement,
            @NonNull LevelReader level,
            @NonNull BlockPos pos,
            @Nullable EntityType<?> type,
            @NonNull CallbackInfoReturnable<Boolean> cir
    ) {
        if (type == null) return;

        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (id == null || !SpawnChanges.TWEAKED_ENTITIES.contains(id.toString())) {
            return;
        }

        boolean isWaterMob = type.getCategory() == MobCategory.WATER_CREATURE
                || type.getCategory() == MobCategory.WATER_AMBIENT
                || type.getCategory() == MobCategory.UNDERGROUND_WATER_CREATURE;

        if (isWaterMob) {
            if (!level.getFluidState(pos).is(FluidTags.WATER)
                    && !level.getFluidState(pos.below()).is(FluidTags.WATER)) {
                cir.setReturnValue(false);
            }
        } else {
            if (level.getFluidState(pos).is(FluidTags.WATER)
                    || level.getFluidState(pos.below()).is(FluidTags.WATER)) {
                cir.setReturnValue(false);
            }
        }
    }
}